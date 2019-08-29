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

import javax.validation.Valid;

import com.karumien.cloud.sso.api.model.DriverInfo;
import com.karumien.cloud.sso.api.model.DriverPin;

/**
 * 
 * DriverService interface that define functionf for drivers api
 *
 * @author <a href="viliam.litavec@karumien.com">Viliam Litavec</a>
 * @since 1.0, 23. 8. 2019
 */
public interface DriverService {

    /**
     * Function to create driver user
     * 
     * @param driver
     *            {@link DriverBaseInfo} object driver that we want to create
     * @return {@link DriverBaseInfo} object of created user
     */
    DriverInfo createDriver(@Valid DriverInfo driver);

    /**
     * Function to set new pin code for the driver
     * 
     * @param id
     *            {@link String} id of the driver for we want to set up pin
     * @param pin
     *            {@link DriverPIN} pin object that we want to set up
     */
    void createPinForTheDriver(String id, @Valid DriverPin pin);

    /**
     * Function to delete user whith role Driver
     * 
     * @param id
     *            {@link String} id of the user
     */
    void deleteDriverUser(String id);
}
