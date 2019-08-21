/**
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.karumien.cloud.sso.service;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.karumien.cloud.sso.api.model.VersionInfo;

import lombok.extern.slf4j.Slf4j;

/**
 * Implementation of {@link InfoService} for aplication info informations show.
 *
 * @author <a href="miroslav.svoboda@karumien.com">Miroslav Svoboda</a>
 * @since 1.0, 19. 8. 2019 12:58:57
 */
@Slf4j
@Service
public class InfoServiceImpl implements InfoService {

    @Value("${spring.application.name}")
    private String applicationName;
    
    protected String getApplicationVersion(String applicationName, List<Attributes> manifests) {

        String unknownVersion = "0.0.0-UNKNOWN";

        for (Attributes attr : manifests) {
            String title = attr.getValue(IMPL_TITLE);
            String version = attr.getValue(IMPL_VERSION);
            if (version != null) {
                if (applicationName.equalsIgnoreCase(title)) {
                    return title + ' ' + version;
                }
            }
        }
        log.warn("Could not find MANIFEST file with '" + applicationName + "' as Implementation-Title." + " Meta-API will return buildVersion '"
                + unknownVersion + "'.");

        return applicationName + ' ' + unknownVersion;
    }

    protected List<Attributes> loadManifestFiles() {
        List<Attributes> manifests = new ArrayList<>();
        try {
            Enumeration<URL> resources = Thread.currentThread().getContextClassLoader().getResources("/META-INF/MANIFEST.MF");
            while (resources.hasMoreElements()) {
                URL url = resources.nextElement();
                try (InputStream is = url.openStream()) {
                    manifests.add(new Manifest(is).getMainAttributes());
                    log.info("Manifest size:" + manifests.size());
                } catch (IOException e) {
                    log.error("Failed to read manifest from " + url, e);
                }
            }
        } catch (IOException e) {
            log.error("Failed to get manifest resources", e);
        }
        return manifests;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public VersionInfo getVersionInfo() {
        List<Attributes> values = loadManifestFiles();
        return VersionInfo.builder()
                .attributes(values).application(getApplicationVersion(applicationName, values)).build();
    }


}
