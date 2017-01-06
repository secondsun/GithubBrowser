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
package org.ajug.githubbrowser;

import org.ajug.githubbrowser.util.GHRepositoryLabelRenderer;
import java.awt.Desktop;
import java.awt.event.InputEvent;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Date;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.awt.Color;

import javax.swing.DefaultListModel;
import javax.swing.JEditorPane;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.html.HTMLEditorKit;
import org.ajug.githubbrowser.service.GitHubService;
import org.ajug.githubbrowser.util.Clipboard;
import org.ajug.githubbrowser.util.GHRepositoryLabel;
import org.ajug.githubbrowser.util.GHRepositoryListModel;
import org.ajug.githubbrowser.util.Pair;
import org.kohsuke.github.GHCommit;
import org.kohsuke.github.GHRepository;

/**
 *
 * @author summers
 */
public class MainController {

    private final Map<String, Pair<String, String>> flagsMap = new TreeMap<>();
    private final Main main;

    ListSelectionListener listener = new ListSelectionListener() {
        @Override
        public void valueChanged(ListSelectionEvent e) {
            GHRepositoryLabel repoLabel = main.getRepositoryList().getSelectedValuesList().get(0);
            updateReadMeAndLicense(repoLabel.getRepo());
        }
    };
    private Object lastUrl;

    public MainController(Main main) {
        this.main = main;
    }

    public void updateRepositories(String organization) {
        GitHubService.getRepositories(organization).thenAccept((map) -> {
            JList<GHRepositoryLabel> list = main.getRepositoryList();
            GHRepositoryListModel model = new GHRepositoryListModel();
            
            StringBuilder repoString = new StringBuilder();
            
            map.values().parallelStream().forEach((repo) -> {
                if (!repo.isPrivate()) {
                    GHRepositoryLabel label = new GHRepositoryLabel(repo);
                    try {
                        String masterCommit = repo.getBranch("master").getSHA1();
                        long lastYear = new Date().getTime() - (1000l * 60 * 60 *24 * 365);
                        GHCommit mostRecentMasterCommit = repo.getCommit(masterCommit);
                        if (mostRecentMasterCommit.getCommitDate().getTime() < lastYear) {
                            label.setStatus(Color.RED);
                        }
                    } catch (Throwable ex) {
                        Logger.getLogger(MainController.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    
                    model.add(label);
                    
                    repoString.append(repo.getName()).append("\n");
                }
                
            });

            Clipboard.copy(model.getRichText());
            model.sort();
            list.setModel(model);
        }).thenAccept((ignore) -> {
            main.getRepositoryList().getSelectionModel().addListSelectionListener(listener);
            main.invalidate();
        }).exceptionally((ex) -> {
            JOptionPane.showMessageDialog(main, ex.getMessage(), "Error", JOptionPane.ERROR);
            return null;
        });
    }

    public void updateReadMeAndLicense(GHRepository repository) {

        main.getRepositoryList().removeListSelectionListener(listener);

        final HTMLEditorKit readmeKit = new HTMLEditorKit();
        final JEditorPane readMePane = main.getReadmePane();
        readMePane.setEditable(false);
        readMePane.setEditorKit(readmeKit);
        
        Document readmeDoc = readmeKit.createDefaultDocument();
        readMePane.setDocument(readmeDoc);

        readMePane.addHyperlinkListener((HyperlinkEvent ev) -> {
            if (HyperlinkEvent.EventType.ACTIVATED.equals(ev.getEventType())) {
                if ((ev.getInputEvent().getModifiers() & InputEvent.BUTTON1_MASK) > 0) {
                    try {
                        if (!ev.getURL().equals(lastUrl)) {
                            Desktop.getDesktop().browse(ev.getURL().toURI());
                            lastUrl = ev.getURL();
                        }
                    } catch (IOException | URISyntaxException ex) {
                        Logger.getLogger(MainController.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    ev.getInputEvent().consume();
                } else if ((ev.getInputEvent().getModifiers() & InputEvent.BUTTON2_MASK) > 0) {
                    Clipboard.copy(ev.getURL());
                    ev.getInputEvent().consume();
                }
            }
        });
        
        final HTMLEditorKit licenseKit = new HTMLEditorKit();
        final JEditorPane licensePane = main.getLicensePane();
        licensePane.setEditable(false);
        licensePane.setEditorKit(licenseKit);
        Document licenseDoc = licenseKit.createDefaultDocument();
        licensePane.setDocument(licenseDoc);

        CompletableFuture.allOf(
                GitHubService.getReadme(repository).thenAccept((readMeReader) -> {
            try {
                readmeKit.read(readMeReader, readmeDoc, 0);
            } catch (IOException | BadLocationException ex) {
                readMePane.setText(ex.getMessage());
            }
        }).exceptionally((Throwable t) -> {
            readMePane.setText(t.getMessage());
            return null;
        }),
                GitHubService.getLicense(repository).thenAccept((readMeReader) -> {
            try {
                licenseKit.read(readMeReader, licenseDoc, 0);
            } catch (IOException | BadLocationException ex) {
                licensePane.setText(ex.getMessage());
            }
        }).exceptionally((Throwable t) -> {
            licensePane.setText(t.getMessage());
            return null;
        })
        ).thenAccept((ignore) -> {

            main.invalidate();
        }).exceptionally((ignore) -> {

            main.invalidate();
            return null;
        });

    }

    void flagReadMe() {
        if (main.getRepositoryList().getSelectedValue() != null) {
            String repoName = main.getRepositoryList().getSelectedValue().toString();
            Pair<String, String> currentPair = flagsMap.getOrDefault(repoName, new Pair<>("", ""));
            flagsMap.put(repoName, new Pair<>("x", currentPair.second));
        }
    }

    void flagLicense() {
        if (main.getRepositoryList().getSelectedValue() != null) {
            String repoName = main.getRepositoryList().getSelectedValue().toString();
            Pair<String, String> currentPair = flagsMap.getOrDefault(repoName, new Pair<>("", ""));
            flagsMap.put(repoName, new Pair<>(currentPair.first, "x"));
        }
    }

    void export() {
        flagsMap.forEach((key, value) -> {
            System.out.println(key + "," + value.first + "," + value.second);
        });
    }

}
