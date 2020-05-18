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
/**
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.karumien.cloud.sso.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.karumien.cloud.sso.api.entity.RebirthEntity;
import com.karumien.cloud.sso.api.repository.RebirthEntityRepository;
import com.karumien.cloud.sso.exceptions.RebirthNotFoundException;

/**
 * Implementation {@link RebirthService} for Account Management.
 *
 * @author <a href="viliam.litavec@karumien.com">Viliam Litavec</a>
 * @since 1.0, 17. 5. 2020 18:59:57
 */
@Service
public class RebirthServiceImpl implements RebirthService {

    @Autowired
    private RebirthEntityRepository rebirthEntityRepository;
    
    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public RebirthEntity createRebirth(RebirthEntity rebirthEntity) {
        return rebirthEntityRepository.save(rebirthEntity);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public RebirthEntity getRebirth(String nav4Id) {
        return rebirthEntityRepository.findById(nav4Id).orElseThrow(() -> new RebirthNotFoundException(nav4Id));
    }
    
}
