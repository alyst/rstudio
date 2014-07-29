/*
 * ViewerPaneSaveAsImageDesktopOperation.java
 *
 * Copyright (C) 2009-12 by RStudio, Inc.
 *
 * Unless you have received this program directly from RStudio pursuant
 * to the terms of a commercial license agreement with RStudio, then
 * this program is licensed to you under the terms of version 3 of the
 * GNU Affero General Public License. This program is distributed WITHOUT
 * ANY EXPRESS OR IMPLIED WARRANTY, INCLUDING THOSE OF NON-INFRINGEMENT,
 * MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE. Please refer to the
 * AGPL (http://www.gnu.org/licenses/agpl-3.0.txt) for more details.
 *
 */

package org.rstudio.studio.client.workbench.views.viewer.export;

import org.rstudio.core.client.Rectangle;
import org.rstudio.core.client.files.FileSystemItem;
import org.rstudio.core.client.widget.MessageDialog;
import org.rstudio.core.client.widget.Operation;
import org.rstudio.core.client.widget.OperationWithInput;
import org.rstudio.core.client.widget.ProgressIndicator;
import org.rstudio.studio.client.application.Desktop;
import org.rstudio.studio.client.common.GlobalDisplay;
import org.rstudio.studio.client.workbench.exportplot.ExportPlotSizeEditor;
import org.rstudio.studio.client.workbench.exportplot.SavePlotAsImageOperation;

public class ViewerPaneSaveAsImageDesktopOperation implements SavePlotAsImageOperation
{
   public ViewerPaneSaveAsImageDesktopOperation(GlobalDisplay globalDisplay)
   {
      globalDisplay_ = globalDisplay;
   }
   
   @Override
   public void attemptSave(final ProgressIndicator progressIndicator, 
                           final FileSystemItem targetPath,
                           final String format, 
                           final ExportPlotSizeEditor sizeEditor,
                           final boolean overwrite, 
                           final boolean viewAfterSave,
                           final Operation onCompleted)
   {
      DesktopExport.export(
            sizeEditor, 
            new OperationWithInput<Rectangle>() {
               @Override
               public void execute(Rectangle viewerRect)
               {
                  // attempt the export
                  boolean exported = Desktop.getFrame().exportPageRegionToFile(
                        targetPath.getPath(), 
                        format, 
                        viewerRect.getLeft(),
                        viewerRect.getTop(),
                        viewerRect.getWidth(),
                        viewerRect.getHeight(),
                        overwrite);
                    
                  if (exported) 
                  {
                     if (viewAfterSave)
                        Desktop.getFrame().showFile(targetPath.getPath());
                  }
                  else if (!overwrite)
                  {
                     globalDisplay_.showYesNoMessage(
                        MessageDialog.WARNING, 
                        "File Exists", 
                        "The specified file name already exists. " +
                        "Do you want to overwrite it?", 
                        new Operation() {
                           @Override
                           public void execute()
                           {
                              attemptSave(progressIndicator,
                                          targetPath,
                                          format,
                                          sizeEditor,
                                          true,
                                          viewAfterSave,
                                          onCompleted);
                           }
                        }, 
                        true);
                  }
               }
            },
            onCompleted
         );  
   }
   
   private final GlobalDisplay globalDisplay_;
}