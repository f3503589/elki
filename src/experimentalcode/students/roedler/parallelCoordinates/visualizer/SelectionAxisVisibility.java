package experimentalcode.students.roedler.parallelCoordinates.visualizer;

import java.util.Collection;

import org.apache.batik.util.SVGConstants;
import org.w3c.dom.Element;
import org.w3c.dom.events.Event;
import org.w3c.dom.events.EventListener;
import org.w3c.dom.events.EventTarget;

import de.lmu.ifi.dbs.elki.data.NumberVector;
import de.lmu.ifi.dbs.elki.result.HierarchicalResult;
import de.lmu.ifi.dbs.elki.result.Result;
import de.lmu.ifi.dbs.elki.result.ResultUtil;
import de.lmu.ifi.dbs.elki.utilities.DatabaseUtil;
import de.lmu.ifi.dbs.elki.visualization.VisualizationTask;
import de.lmu.ifi.dbs.elki.visualization.css.CSSClass;
import de.lmu.ifi.dbs.elki.visualization.projector.ParallelPlotProjector;
import de.lmu.ifi.dbs.elki.visualization.style.StyleLibrary;
import de.lmu.ifi.dbs.elki.visualization.svg.SVGPlot;
import de.lmu.ifi.dbs.elki.visualization.svg.SVGUtil;
import de.lmu.ifi.dbs.elki.visualization.visualizers.AbstractVisFactory;
import de.lmu.ifi.dbs.elki.visualization.visualizers.Visualization;
import de.lmu.ifi.dbs.elki.visualization.visualizers.parallel.AbstractParallelVisualization;

/**
 * Layer for controlling axis visbility in parallel coordinates.
 * 
 * @author Robert Rödler
 */
public class SelectionAxisVisibility extends AbstractParallelVisualization<NumberVector<?, ?>> {
  /**
   * Generic tags to indicate the type of element. Used in IDs, CSS-Classes etc.
   */
  public static final String SELECTAXISVISIBILITY = "SelectAxisVisibility";

  /**
   * CSS class for a tool button
   */
  public static final String SAV_BUTTON = "SAVbutton";

  /**
   * CSS class for a button border
   */
  public static final String SAV_BORDER = "SAVborder";

  /**
   * CSS class for a button cross
   */
  public static final String SAV_CROSS = "SAVbuttoncross";

  private Element border;

  private Element[] rect;

  private int c;

  double hs = getSizeY() / 35.;

  double qs = hs / 2.;

  double cs = hs / 8.;

  double bhs = (getSizeY() / 35.) * 0.75;

  double hbs = bhs / 2.;

  double ypos = getSizeY() + getMarginTop() * 1.5 + hs / 8;

  /**
   * Constructor.
   * 
   * @param task VisualizationTask
   */
  public SelectionAxisVisibility(VisualizationTask task) {
    super(task);
    incrementalRedraw();
    context.addResultListener(this);
  }

  @Override
  public void destroy() {
    context.removeResultListener(this);
    super.destroy();
  }

  @Override
  protected void redraw() {
    addCSSClasses(svgp);
    int dim = DatabaseUtil.dimensionality(relation);

    Element back = svgp.svgRect(-hs / 2, getSizeY() + getMarginTop() * 1.5, getSizeX() + hs, hs);
    SVGUtil.addCSSClass(back, SELECTAXISVISIBILITY);
    layer.appendChild(back);

    int lastvis = 0;
    c = 0;
    rect = new Element[dim];

    for(int i = 0, j = 0; i <= dim; i++) {
      if(i == dim || proj.isAxisVisible(i)) {
        if(i - lastvis > 1) {
          addEmptyButton(lastvis, i, dim);
        }

        if(i == dim) {
          break;
        }

        double xpos = getVisibleAxisX(j) - bhs / 2.;

        border = svgp.svgRect(xpos, ypos, bhs, bhs);
        SVGUtil.addCSSClass(border, SAV_BORDER);
        layer.appendChild(border);

        Element cross = svgp.svgLine(xpos + cs, ypos + cs, xpos + cs + qs, ypos + cs + qs);
        SVGUtil.addCSSClass(cross, SAV_CROSS);
        layer.appendChild(cross);

        Element cross2 = svgp.svgLine(xpos + cs + qs, ypos + cs, xpos + cs, ypos + cs + qs);
        SVGUtil.addCSSClass(cross2, SAV_CROSS);
        layer.appendChild(cross2);

        rect[c] = svgp.svgRect(xpos, ypos, bhs, bhs);
        SVGUtil.addCSSClass(rect[c], SAV_BUTTON);
        addEventListener(rect[c], c);
        layer.appendChild(rect[c]);

        lastvis = i;
        c++;
        j++;
      }
    }
  }

  private void addEmptyButton(int last, int vis, int dim) {
    int notvis = (vis - last) - 1;
    if(notvis > 2 && (vis == 0 || vis == dim)) {
      double dist = (cs + qs) * 2.;

      if(vis == dim) {
        for(int j = 0; j < notvis; j++) {
          border = svgp.svgRect(getVisibleAxisX(last) + dist - hbs, ypos - j * dist, bhs, bhs);
          SVGUtil.addCSSClass(border, SAV_BORDER);
          layer.appendChild(border);

          rect[c] = svgp.svgRect(getVisibleAxisX(last) + dist - hbs, ypos - j * dist, bhs, bhs);
          SVGUtil.addCSSClass(rect[c], SAV_BUTTON);
          addEventListener(rect[c], c);
          layer.appendChild(rect[c]);
          c++;
        }
      }
      else {
        double xpos = 0.;
        for(int j = 0; j < notvis; j++) {
          border = svgp.svgRect(xpos + dist - hbs, (ypos - (notvis - 1) * dist) + j * dist, bhs, bhs);
          SVGUtil.addCSSClass(border, SAV_BORDER);
          layer.appendChild(border);

          rect[c] = svgp.svgRect(xpos + dist - hbs, (ypos - (notvis - 1) * dist) + j * dist, bhs, bhs);
          SVGUtil.addCSSClass(rect[c], SAV_BUTTON);
          addEventListener(rect[c], c);
          layer.appendChild(rect[c]);
          c++;
        }
      }
    }
    else {
      double xpos = getVisibleAxisX(last);
      if(xpos < 0.) {
        xpos = 0.;
      }
      double dist;
      if(vis == dim) {
        dist = getMarginLeft() / (notvis + 1.);
      }
      else {
        dist = (getVisibleAxisX(vis) - xpos) / (notvis + 1.);
      }

      for(int j = 0; j < notvis; j++) {
        border = svgp.svgRect(xpos + (1 + j) * dist - hbs, ypos, bhs, bhs);
        SVGUtil.addCSSClass(border, SAV_BORDER);
        layer.appendChild(border);

        rect[c] = svgp.svgRect(xpos + (1 + j) * dist - hbs, ypos, bhs, bhs);
        SVGUtil.addCSSClass(rect[c], SAV_BUTTON);
        addEventListener(rect[c], c);
        layer.appendChild(rect[c]);
        c++;
      }
    }
  }

  /**
   * Add an event listener to the Element
   * 
   * @param tag Element to add the listener
   * @param tool Tool represented by the Element
   */
  private void addEventListener(final Element tag, final int i) {
    EventTarget targ = (EventTarget) tag;
    targ.addEventListener(SVGConstants.SVG_EVENT_CLICK, new EventListener() {
      @Override
      public void handleEvent(Event evt) {
        if(proj.getVisibleDimensions() > 2) {
          proj.toggleAxisVisible(i);
          context.getHierarchy().resultChanged(proj);
        }
      }
    }, false);
  }

  /**
   * Adds the required CSS-Classes
   * 
   * @param svgp SVG-Plot
   */
  private void addCSSClasses(SVGPlot svgp) {
    final StyleLibrary style = context.getStyleLibrary();
    if(!svgp.getCSSClassManager().contains(SELECTAXISVISIBILITY)) {
      CSSClass cls = new CSSClass(this, SELECTAXISVISIBILITY);
      cls.setStatement(SVGConstants.CSS_OPACITY_PROPERTY, 0.1);
      cls.setStatement(SVGConstants.CSS_FILL_PROPERTY, SVGConstants.CSS_BLUE_VALUE);
      svgp.addCSSClassOrLogError(cls);
    }
    if(!svgp.getCSSClassManager().contains(SAV_BORDER)) {
      CSSClass cls = new CSSClass(this, SAV_BORDER);
      cls.setStatement(SVGConstants.CSS_STROKE_PROPERTY, SVGConstants.CSS_GREY_VALUE);
      cls.setStatement(SVGConstants.CSS_STROKE_WIDTH_PROPERTY, style.getLineWidth(StyleLibrary.PLOT) * .5);
      cls.setStatement(SVGConstants.CSS_FILL_PROPERTY, SVGConstants.CSS_NONE_VALUE);
      svgp.addCSSClassOrLogError(cls);
    }
    if(!svgp.getCSSClassManager().contains(SAV_BUTTON)) {
      CSSClass cls = new CSSClass(this, SAV_BUTTON);
      cls.setStatement(SVGConstants.CSS_OPACITY_PROPERTY, 0.01);
      cls.setStatement(SVGConstants.CSS_FILL_PROPERTY, SVGConstants.CSS_GREY_VALUE);
      svgp.addCSSClassOrLogError(cls);
    }
    if(!svgp.getCSSClassManager().contains(SAV_CROSS)) {
      CSSClass cls = new CSSClass(this, SAV_CROSS);
      cls.setStatement(SVGConstants.CSS_STROKE_PROPERTY, SVGConstants.CSS_BLACK_VALUE);
      cls.setStatement(SVGConstants.CSS_STROKE_WIDTH_PROPERTY, style.getLineWidth(StyleLibrary.PLOT) * .75);
      cls.setStatement(SVGConstants.CSS_FILL_PROPERTY, SVGConstants.CSS_NONE_VALUE);
      svgp.addCSSClassOrLogError(cls);
    }
  }

  /**
   * Factory for axis visualizations
   * 
   * @author Erich Schubert
   * 
   * @apiviz.stereotype factory
   * @apiviz.uses AxisVisualization oneway - - «create»
   * 
   */
  public static class Factory extends AbstractVisFactory {
    /**
     * A short name characterizing this Visualizer.
     */
    private static final String NAME = "Selection Axis Visibility";

    /**
     * Constructor, adhering to
     */
    public Factory() {
      super();
    }

    @Override
    public Visualization makeVisualization(VisualizationTask task) {
      return new SelectionAxisVisibility(task);
    }

    @Override
    public void processNewResult(HierarchicalResult baseResult, Result result) {
      Collection<ParallelPlotProjector<?>> ps = ResultUtil.filterResults(result, ParallelPlotProjector.class);
      for(ParallelPlotProjector<?> p : ps) {
        final VisualizationTask task = new VisualizationTask(NAME, p, p.getRelation(), this);
        task.put(VisualizationTask.META_LEVEL, VisualizationTask.LEVEL_INTERACTIVE);
        task.put(VisualizationTask.META_NOEXPORT, true);
        task.put(VisualizationTask.META_NOTHUMB, true);
        baseResult.getHierarchy().add(p, task);
      }
    }

    @Override
    public boolean allowThumbnails(VisualizationTask task) {
      // Don't use thumbnails
      return false;
    }
  }
}
