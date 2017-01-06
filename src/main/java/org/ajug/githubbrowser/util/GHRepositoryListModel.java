/**
 * GithubBrowser - ${project.description}
 * Copyright © ${project.inceptionYear} SecondSun (Summers Pittman) (secondsun@gmail.com)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ajug.githubbrowser.util;

import java.awt.Color;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.AbstractListModel;

/**
 *
 * @author summers
 */
public class GHRepositoryListModel extends AbstractListModel<GHRepositoryLabel> {

    ArrayList<GHRepositoryLabel> labels = new ArrayList<>();

    public GHRepositoryListModel() {
    }

    
    
    @Override
    public int getSize() {
        return labels.size();
    }

    @Override
    public GHRepositoryLabel getElementAt(int index) {
        return labels.get(index);
    }

    public void sort() {
        Collections.sort(labels);
        fireContentsChanged(this, 0, labels.size());
    }

    public void add(GHRepositoryLabel label) {
        labels.add(label);
    }

    public Transferable getRichText() {
        return new RichTextTransferrable(labels);
    }

    private static final class RichTextTransferrable implements Transferable {

        private final ArrayList<GHRepositoryLabel> labels;

        private RichTextTransferrable(ArrayList<GHRepositoryLabel> labels) {
            this.labels = labels;
        }

        @Override
        public DataFlavor[] getTransferDataFlavors() {
            try {
                return new DataFlavor[] {new DataFlavor("text/rtf")};
            } catch (ClassNotFoundException ex) {
                throw new RuntimeException(ex);
            }
        }

        @Override
        public boolean isDataFlavorSupported(DataFlavor flavor) {
            return flavor.getMimeType().contains("text/rtf");
        }

        @Override
        public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException {
            if (isDataFlavorSupported(flavor)) {
                StringBuilder builder = new StringBuilder(labels.size() * 64);
                builder.append("{\\rtf1\\ansi\\deff0 {\\fonttbl {\\f0 Courier;}}\r\n");
                builder.append("{\\colortbl;\\red0\\green0\\blue0;\\red255\\green0\\blue0;}\r\n");
                labels.forEach((label) -> {
                    if (label.getStatus() == Color.RED) {
                        builder.append("{\\cf2\r\n");
                    }  else {
                        builder.append("{\\cf1\r\n");
                    }
                    builder.append(label.getRepo().getName());
                    builder.append("\\line\r\n");
                });
                builder.append("}");
                return new ByteArrayInputStream(builder.toString().getBytes());
            } else {
                throw new UnsupportedFlavorException(flavor);
            }
        }
    }
    
}


