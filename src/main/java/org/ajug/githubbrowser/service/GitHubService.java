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
package org.ajug.githubbrowser.service;

import java.io.IOException;
import java.io.Reader;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.apache.commons.io.IOUtils;
import org.kohsuke.github.GHOrganization;
import org.kohsuke.github.GHRepository;
import org.kohsuke.github.GitHub;

public class GitHubService {

    private static final ExecutorService EXECUTOR = Executors.newWorkStealingPool();
    private static final GitHub GITHUB;

    static {
        try {
            GITHUB = GitHub.connect();
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    public static CompletableFuture<Map<String, GHRepository>> getRepositories(String organizationName) {

        return CompletableFuture.supplyAsync(() -> {
            try {
                GHOrganization org = GITHUB.getOrganization(organizationName);
                return org.getRepositories();
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        }, EXECUTOR);

    }

    public static CompletableFuture<Reader> getReadme(GHRepository repo) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return GITHUB.renderMarkdown(IOUtils.toString(repo.getReadme().read()));
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        }, EXECUTOR);

    }

    public static CompletableFuture<Reader> getLicense(GHRepository repo) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return GITHUB.renderMarkdown(IOUtils.toString(repo.getLicenseContent().read()));
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        }, EXECUTOR);

    }

}
