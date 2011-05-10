package experimentalcode.frankenb.model;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import de.lmu.ifi.dbs.elki.data.DoubleVector;
import de.lmu.ifi.dbs.elki.data.NumberVector;
import de.lmu.ifi.dbs.elki.utilities.pairs.Pair;
import experimentalcode.frankenb.model.ifaces.IPartition;

/**
 * A part of a database that is normally used in a package for precalculating
 * distances for knn. This datatype is backed up on disk but held in memory until it is
 * closed - it should be suitable for most amounts of datasets.
 * 
 * @author Florian Frankenberger
 */
public class BufferedDiskBackedPartition implements IPartition {

  private int id;
  private final File storageFile;
  private int dimensionality;
  
  private List<Pair<Integer, NumberVector<?, ?>>> entries = new ArrayList<Pair<Integer, NumberVector<?, ?>>>();
  
  public BufferedDiskBackedPartition(int id, int dimensionality) {
    this(id, dimensionality, null);
  }
  
  private BufferedDiskBackedPartition(int id, int dimensionality, File storageFile) {
    if (storageFile == null && dimensionality < 1) {
      throw new RuntimeException("You need to specify a dimensionality if you don't load a partition from disk");
    }
    
    this.id = id;
    this.dimensionality = dimensionality;
    
    if (storageFile == null) {
      try {
        storageFile = File.createTempFile("partition_", null);
        storageFile.delete();
      } catch (IOException e) {
        throw new RuntimeException("Could not create temporary storage file for partition", e);
      }
    }
    this.storageFile = storageFile;
    readAll();
  }
  
  @Override
  public int getId() {
    return this.id;
  }
  
  private void readAll() {
    if (storageFile == null || !storageFile.exists() || !storageFile.canRead()) return;
    
    try {
      entries.clear();
      RandomAccessFile file = null;
      try {
        file = new RandomAccessFile(this.storageFile, "r");
        file.seek(0);
        this.id = file.readInt();
        this.dimensionality = file.readInt();
        
        while (file.getFilePointer() < this.storageFile.length()-1) {
          
          int id = file.readInt();
          
          double[] data = new double[dimensionality];
          for (int k = 0; k < dimensionality; ++k) {
            data[k] = file.readDouble();
          }
          
          entries.add(new Pair<Integer, NumberVector<?, ?>>(id, new DoubleVector(data)));
        }
      } finally {
        if (file != null) {
          file.close();
        }
      }
    } catch (IOException e) {
      throw new RuntimeException("Could not open partition file", e);
    }
  }
  
  private void writeAll() {
    try {
      RandomAccessFile file = null;
      try {
        file = new RandomAccessFile(this.storageFile, "rw");
        file.seek(0);
        file.writeInt(this.id);
        file.writeInt(this.dimensionality);
        for (Pair<Integer, NumberVector<?, ?>> entry : entries) {
          
          file.writeInt(entry.first);
          
          for (int k = 0; k < dimensionality; ++k) {
            file.writeDouble(entry.second.doubleValue(k + 1));
          }
          
        }
      } finally {
        if (file != null) {
          file.close();
        }
        readAll();
      }
    } catch (IOException e) {
      throw new RuntimeException("Could not open partition file", e);
    }
  }
  
  @Override
  public File getStorageFile() {
    return this.storageFile;
  }
  
  @Override
  public void addVector(int id, NumberVector<?, ?> vector) {
    this.entries.add(new Pair<Integer, NumberVector<?, ?>>(id, vector));
  }
  
  @Override
  public void close() throws IOException {
    writeAll();
  }
  
  @Override
  public Iterator<Pair<Integer, NumberVector<?, ?>>> iterator() {
    return new Iterator<Pair<Integer, NumberVector<?, ?>>>() {

      private int position = 0;
      
      @Override
      public boolean hasNext() {
        return position < entries.size();
      }

      @Override
      public Pair<Integer, NumberVector<?, ?>> next() {
        return entries.get(position++);
      }

      @Override
      public void remove() {
        throw new UnsupportedOperationException();
      }
      
    };
  }
  
  @Override
  public int getSize() {
    return entries.size();
  }
  
  @Override
  public int getDimensionality() {
    return this.dimensionality;
  }
  
  @Override
  public void copyTo(File file) throws IOException {
    writeAll();
    
    InputStream in = null;
    OutputStream out = null;
    try {
      in = new FileInputStream(this.storageFile);
      out = new FileOutputStream(file);
      
      int read = 0;
      byte[] buffer = new byte[1024];
      
      while ((read = in.read(buffer)) != -1) {
        out.write(buffer, 0, read);
      }
      
    } finally {
      if (in != null) in.close();
      if (out != null) out.close();
    }
    
  }
  
  public static BufferedDiskBackedPartition loadFromFile(File file) throws IOException {
    if (!file.exists()) throw new IOException("Specified partition file " + file + " does not exist!");
    return new BufferedDiskBackedPartition(0, 0, file);
  }
  
}
