package experimentalcode.students.roedler.parallelCoordinates.visualizer;

/*
This file is part of ELKI:
Environment for Developing KDD-Applications Supported by Index-Structures

Copyright (C) 2012
Ludwig-Maximilians-Universität München
Lehr- und Forschungseinheit für Datenbanksysteme
ELKI Development Team

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/

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
import de.lmu.ifi.dbs.elki.visualization.VisualizationTask;
import de.lmu.ifi.dbs.elki.visualization.css.CSSClass;
import de.lmu.ifi.dbs.elki.visualization.projector.ParallelPlotProjector;
import de.lmu.ifi.dbs.elki.visualization.style.StyleLibrary;
import de.lmu.ifi.dbs.elki.visualization.svg.SVGPath;
import de.lmu.ifi.dbs.elki.visualization.svg.SVGPlot;
import de.lmu.ifi.dbs.elki.visualization.svg.SVGUtil;
import de.lmu.ifi.dbs.elki.visualization.visualizers.AbstractVisFactory;
import de.lmu.ifi.dbs.elki.visualization.visualizers.Visualization;
import de.lmu.ifi.dbs.elki.visualization.visualizers.parallel.AbstractParallelVisualization;

/**
 * interactive SVG-Element for selecting visible axis.
 * 
 * @author Robert Rödler
 */
public class DimensionOrderVisualization extends AbstractParallelVisualization<NumberVector<?, ?>> {
  /**
   * Generic tags to indicate the type of element. Used in IDs, CSS-Classes etc.
   */
  public static final String SELECTDIMENSIONORDER = "SelectDimensionOrder";

  /**
   * CSS class for a tool button
   */
  public static final String SDO_BUTTON = "DObutton";

  /**
   * CSS class for a button border
   */
  public static final String SDO_BORDER = "DOborder";

  /**
   * CSS class for a button cross
   */
  public static final String SDO_ARROW = "DOarrow";

  private int selecteddim = -1;

  private boolean selected = false;

  /**
   * Constructor.
   * 
   * @param task VisualizationTask
   */
  public DimensionOrderVisualization(VisualizationTask task) {
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
    final int dim = proj.getVisibleDimensions();

    final double as = getSizeY() / 70.;
    final double bs = as * 1.5;
    final double hbs = bs / 2.;
    final double qas = as / 4.;
    final double ypos = getSizeY() + getMarginTop() * 2.;
    final double dist = 2.5 * as;

    Element back = svgp.svgRect(0.0, getSizeY() + getMarginTop() * 2., getSizeX(), getSizeY() / 35.);
    SVGUtil.addCSSClass(back, SELECTDIMENSIONORDER);
    layer.appendChild(back);

    if(!selected) {
      for(int i = 0; i < dim; i++) {
        if(i > 0) {
          int j = 0;
          Element arrow = makeArrow(Direction.LEFT, (getVisibleAxisX(i) - dist) + j * dist, ypos + as, as);
          SVGUtil.addCSSClass(arrow, SDO_ARROW);
          layer.appendChild(arrow);
          Element button = svgp.svgRect((getVisibleAxisX(i) - (dist + hbs)) + j * dist, ypos + qas, bs, bs);
          SVGUtil.addCSSClass(button, SDO_BUTTON);
          addEventListener(button, i, Direction.LEFT);
          layer.appendChild(button);
        }
        {
          int j = 1;
          Element arrow = makeArrow(Direction.DOWN, (getVisibleAxisX(i) - dist) + j * dist, ypos + as, as);
          SVGUtil.addCSSClass(arrow, SDO_ARROW);
          layer.appendChild(arrow);
          Element button = svgp.svgRect((getVisibleAxisX(i) - (dist + hbs)) + j * dist, ypos + qas, bs, bs);
          SVGUtil.addCSSClass(button, SDO_BUTTON);
          addEventListener(button, i, Direction.DOWN);
          layer.appendChild(button);
        }
        if(i < dim - 1) {
          int j = 2;
          Element arrow = makeArrow(Direction.RIGHT, (getVisibleAxisX(i) - dist) + j * dist, ypos + as, as);
          SVGUtil.addCSSClass(arrow, SDO_ARROW);
          layer.appendChild(arrow);
          Element button = svgp.svgRect((getVisibleAxisX(i) - (dist + hbs)) + j * dist, ypos + qas, bs, bs);
          SVGUtil.addCSSClass(button, SDO_BUTTON);
          addEventListener(button, i, Direction.RIGHT);
          layer.appendChild(button);
        }
      }
    }
    else {
      for(int i = 0; i < dim; i++) {
        {
          Element arrow = makeArrow(Direction.SWAPWITH, getVisibleAxisX(i), ypos + as, as);
          SVGUtil.addCSSClass(arrow, SDO_ARROW);
          layer.appendChild(arrow);
          Element button = svgp.svgRect(getVisibleAxisX(i) - hbs, ypos + qas, bs, bs);
          SVGUtil.addCSSClass(button, SDO_BUTTON);
          addEventListener(button, i, Direction.SWAPWITH);
          layer.appendChild(button);
        }
        if(i > 0.) {
          Element arrow = makeArrow(Direction.INSERT, getVisibleAxisX(i - .5), ypos + as, as);
          SVGUtil.addCSSClass(arrow, SDO_ARROW);
          layer.appendChild(arrow);
          Element button = svgp.svgRect(getVisibleAxisX(i - .5) - hbs, ypos + qas, bs, bs);
          SVGUtil.addCSSClass(button, SDO_BUTTON);
          addEventListener(button, i, Direction.INSERT);
          layer.appendChild(button);
        }
      }
    }
  }

  /**
   * Add an event listener to the Element
   * 
   * @param tag Element to add the listener
   * @param i represented axis
   */
  private void addEventListener(final Element tag, final int i, final Direction j) {
    EventTarget targ = (EventTarget) tag;
    targ.addEventListener(SVGConstants.SVG_EVENT_CLICK, new EventListener() {
      @Override
      public void handleEvent(Event evt) {
        if(j == Direction.DOWN) {
          selected = true;
          selecteddim = i;
        }
        if(j == Direction.SWAPWITH || j == Direction.INSERT) {
          if(j == Direction.SWAPWITH) {
            proj.swapAxes(selecteddim, i);
          }
          else {
            if(selecteddim != i) {
              proj.moveAxis(selecteddim, i);
            }
          }
          selected = false;
          selecteddim = -1;
        }
        if(j == Direction.LEFT || j == Direction.RIGHT) {
          if(j == Direction.LEFT) {
            int prev = i - 1;
            while(prev >= 0 && !proj.isAxisVisible(prev)) {
              prev -= 1;
            }
            proj.swapAxes(i, prev);
          }
          else {
            int next = i + 1;
            while(next < proj.getInputDimensionality() - 1 && !proj.isAxisVisible(next)) {
              next += 1;
            }
            proj.swapAxes(i, next);
          }
        }
        // Notify
        context.getHierarchy().resultChanged(proj);
      }
    }, false);
  }

  // Constants for arrow directions and insertion positions
  private enum Direction {
    LEFT, DOWN, RIGHT, UP, SWAPWITH, INSERT
  }

  private Element makeArrow(Direction dir, double x, double y, double size) {
    final SVGPath path = new SVGPath();
    final double hs = size / 2.;

    switch(dir){
    case LEFT:
      path.drawTo(x + hs, y + hs);
      path.drawTo(x - hs, y);
      path.drawTo(x + hs, y - hs);
      path.drawTo(x + hs, y + hs);
      break;
    case DOWN:
      path.drawTo(x - hs, y - hs);
      path.drawTo(x + hs, y - hs);
      path.drawTo(x, y + hs);
      path.drawTo(x - hs, y - hs);
      break;
    case RIGHT:
      path.drawTo(x - hs, y - hs);
      path.drawTo(x + hs, y);
      path.drawTo(x - hs, y + hs);
      path.drawTo(x - hs, y - hs);
      break;
    case UP:
    case SWAPWITH:
    case INSERT:
      path.drawTo(x - hs, y + hs);
      path.drawTo(x, y - hs);
      path.drawTo(x + hs, y + hs);
      path.drawTo(x - hs, y + hs);
      break;
    }
    path.close();
    return path.makeElement(svgp);
  }

  /**
   * Adds the required CSS-Classes
   * 
   * @param svgp SVG-Plot
   */
  private void addCSSClasses(SVGPlot svgp) {
    final StyleLibrary style = context.getStyleLibrary();
    if(!svgp.getCSSClassManager().contains(SELECTDIMENSIONORDER)) {
      CSSClass cls = new CSSClass(this, SELECTDIMENSIONORDER);
      cls.setStatement(SVGConstants.CSS_OPACITY_PROPERTY, 0.1);
      cls.setStatement(SVGConstants.CSS_FILL_PROPERTY, SVGConstants.CSS_BLUE_VALUE);
      svgp.addCSSClassOrLogError(cls);
    }
    if(!svgp.getCSSClassManager().contains(SDO_BORDER)) {
      CSSClass cls = new CSSClass(this, SDO_BORDER);
      cls.setStatement(SVGConstants.CSS_STROKE_PROPERTY, SVGConstants.CSS_GREY_VALUE);
      cls.setStatement(SVGConstants.CSS_STROKE_WIDTH_PROPERTY, style.getLineWidth(StyleLibrary.PLOT) / 3.0);
      cls.setStatement(SVGConstants.CSS_FILL_PROPERTY, SVGConstants.CSS_NONE_VALUE);
      svgp.addCSSClassOrLogError(cls);
    }
    if(!svgp.getCSSClassManager().contains(SDO_BUTTON)) {
      CSSClass cls = new CSSClass(this, SDO_BUTTON);
      cls.setStatement(SVGConstants.CSS_OPACITY_PROPERTY, 0.01);
      cls.setStatement(SVGConstants.CSS_FILL_PROPERTY, SVGConstants.CSS_GREY_VALUE);
      svgp.addCSSClassOrLogError(cls);
    }
    if(!svgp.getCSSClassManager().contains(SDO_ARROW)) {
      CSSClass cls = new CSSClass(this, SDO_ARROW);
      cls.setStatement(SVGConstants.CSS_STROKE_PROPERTY, SVGConstants.CSS_DARKGREY_VALUE);
      cls.setStatement(SVGConstants.CSS_STROKE_WIDTH_PROPERTY, style.getLineWidth(StyleLibrary.PLOT) / 2.5);
      cls.setStatement(SVGConstants.CSS_FILL_PROPERTY, SVGConstants.CSS_BLACK_VALUE);
      svgp.addCSSClassOrLogError(cls);
    }
  }

  /**
   * Factory for dimension selection visualizer
   * 
   * @author RobertRödler
   * 
   * @apiviz.stereotype factory
   * @apiviz.uses DimensionOrderVisualization oneway - - «create»
   */
  public static class Factory extends AbstractVisFactory {
    /**
     * A short name characterizing this Visualizer.
     */
    private static final String NAME = "Selection Dimension Order";

    /**
     * Constructor, adhering to
     */
    public Factory() {
      super();
    }

    @Override
    public Visualization makeVisualization(VisualizationTask task) {
      return new DimensionOrderVisualization(task);
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
