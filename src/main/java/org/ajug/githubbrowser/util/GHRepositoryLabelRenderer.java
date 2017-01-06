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

import java.awt.Component;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JList;

/**
 *
 * @author summers
 */
public class GHRepositoryLabelRenderer extends DefaultListCellRenderer {

    public GHRepositoryLabelRenderer() {
    }

    @Override
    public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
        Component toReturn = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);

        GHRepositoryLabel label = (GHRepositoryLabel) value;        
        toReturn.setForeground(label.getStatus());
        return toReturn;
    }
    
}
