/*
 * ******************************************************************************
 *  * Copyright 2015 See AUTHORS file.
 *  *
 *  * Licensed under the Apache License, Version 2.0 (the "License");
 *  * you may not use this file except in compliance with the License.
 *  * You may obtain a copy of the License at
 *  *
 *  *   http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the License is distributed on an "AS IS" BASIS,
 *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  * See the License for the specific language governing permissions and
 *  * limitations under the License.
 *  *****************************************************************************
 */

package com.uwsoft.editor.view.ui.dialog;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector2;
import com.puremvc.patterns.mediator.SimpleMediator;
import com.puremvc.patterns.observer.Notification;
import com.uwsoft.editor.view.frame.FileDropListener;
import com.uwsoft.editor.view.stage.Sandbox;
import com.uwsoft.editor.view.ui.widget.ProgressHandler;
import com.uwsoft.editor.Overlap2DFacade;
import com.uwsoft.editor.proxy.ProjectManager;
import com.uwsoft.editor.view.Overlap2DMenuBar;
import com.uwsoft.editor.view.stage.UIStage;
import com.uwsoft.editor.renderer.data.SceneVO;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.io.File;
import java.util.List;

/**
 * Created by sargis on 4/3/15.
 */
public class ImportDialogMediator extends SimpleMediator<ImportDialog> {
    private static final String TAG = ImportDialogMediator.class.getCanonicalName();
    private static final String NAME = TAG;
    private AssetsImportProgressHandler progressHandler;

    public ImportDialogMediator() {
        super(NAME, new ImportDialog());
    }

    @Override
    public void onRegister() {
        super.onRegister();
        facade = Overlap2DFacade.getInstance();
        progressHandler = new AssetsImportProgressHandler();
    }

    @Override
    public String[] listNotificationInterests() {
        return new String[]{
                Overlap2DMenuBar.IMPORT_TO_LIBRARY,
                ImportDialog.START_IMPORTING_BTN_CLICKED,
                FileDropListener.ACTION_DRAG_ENTER,
                FileDropListener.ACTION_DRAG_OVER,
                FileDropListener.ACTION_DRAG_EXIT,
                FileDropListener.ACTION_DROP,
        };
    }

    public Vector2 getLocationFromDtde(DropTargetDragEvent dtde) {
        Vector2 pos = new Vector2((float)(dtde).getLocation().getX(),(float)(dtde).getLocation().getY());

        return pos;
    }

    @Override
    public void handleNotification(Notification notification) {
        super.handleNotification(notification);
        Sandbox sandbox = Sandbox.getInstance();
        UIStage uiStage = sandbox.getUIStage();
        switch (notification.getName()) {
            case Overlap2DMenuBar.IMPORT_TO_LIBRARY:
                viewComponent.show(uiStage);
                break;
            case FileDropListener.ACTION_DRAG_ENTER:
                Vector2 dropPos = getLocationFromDtde(notification.getBody());
                if(viewComponent.checkDropRegionHit(dropPos)) {
                    viewComponent.dragOver();
                }
                break;
            case FileDropListener.ACTION_DRAG_OVER:
                dropPos = getLocationFromDtde(notification.getBody());
                if(viewComponent.checkDropRegionHit(dropPos)) {
                    viewComponent.dragOver();
                }
                break;
            case FileDropListener.ACTION_DRAG_EXIT:
                dropPos = getLocationFromDtde(notification.getBody());
                if(viewComponent.checkDropRegionHit(dropPos)) {
                    viewComponent.dragExit();
                }
                break;
            case FileDropListener.ACTION_DROP:
                DropTargetDropEvent dtde = notification.getBody();
                String[] paths = catchFiles(dtde);
                viewComponent.setPaths(paths);
                break;
            case ImportDialog.START_IMPORTING_BTN_CLICKED:
                /*
                ProjectManager projectManager = facade.retrieveProxy(ProjectManager.NAME);
                projectManager.importImagesIntoProject(viewComponent.getImageFiles(), progressHandler);
                projectManager.importParticlesIntoProject(viewComponent.getParticleEffectFiles(), progressHandler);
                projectManager.importStyleIntoProject(viewComponent.getStyleFiles(), progressHandler);
                projectManager.importFontIntoProject(viewComponent.getFontFiles(), progressHandler);
                projectManager.importSpineAnimationsIntoProject(viewComponent.getSpineSpriterFiles(), progressHandler);
                projectManager.importSpriteAnimationsIntoProject(viewComponent.getSpriteAnimationFiles(), progressHandler);
                // save before importing
                SceneVO vo = sandbox.sceneVoFromItems();
//                uiStage.getCompositePanel().updateOriginalItem();
                projectManager.saveCurrentProject(vo);*/
                break;
        }
    }

    public String[] catchFiles(DropTargetDropEvent dtde) {
        dtde.acceptDrop(DnDConstants.ACTION_COPY_OR_MOVE);

        Transferable t= dtde.getTransferable();
        if (t.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
            try {
                List<File> list = (List<File>)dtde.getTransferable().getTransferData(DataFlavor.javaFileListFlavor);
                String[] paths = new String[list.size()];
                for(int i = 0; i < list.size(); i++) {
                    paths[i] = list.get(i).getAbsolutePath();
                }
                return paths;
            }
            catch (Exception ufe) {
            }
        }

        return null;
    }

    private class AssetsImportProgressHandler implements ProgressHandler {

        @Override
        public void progressStarted() {

        }

        @Override
        public void progressChanged(float value) {

        }

        @Override
        public void progressComplete() {
            Gdx.app.postRunnable(() -> {
                Sandbox sandbox = Sandbox.getInstance();
                UIStage uiStage = sandbox.getUIStage();
                ProjectManager projectManager = facade.retrieveProxy(ProjectManager.NAME);
                projectManager.openProjectAndLoadAllData(projectManager.getCurrentProjectVO().projectName);
                sandbox.loadCurrentProject();
                ImportDialogMediator.this.viewComponent.hide();
                facade.sendNotification(ProjectManager.PROJECT_DATA_UPDATED);
            });
        }
    }
}
