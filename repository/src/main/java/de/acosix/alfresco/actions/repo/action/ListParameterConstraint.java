/*
 * Copyright 2019 Acosix GmbH
 *
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
package de.acosix.alfresco.actions.repo.action;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.repo.action.constraint.BaseParameterConstraint;
import org.alfresco.util.PropertyCheck;
import org.springframework.beans.factory.InitializingBean;

/**
 *
 * Instances of this class provide allowed values with localisation based on a configured list of values.
 *
 * @author Axel Faust
 */
public class ListParameterConstraint extends BaseParameterConstraint implements InitializingBean
{

    protected List<String> allowedValues;

    /**
     *
     * {@inheritDoc}
     */
    @Override
    public void afterPropertiesSet()
    {
        PropertyCheck.mandatory(this, "allowedValues", this.allowedValues);
    }

    /**
     * @param allowedValues
     *            the allowedValues to set
     */
    public void setAllowedValues(final List<String> allowedValues)
    {
        this.allowedValues = allowedValues;
    }

    /**
     *
     * {@inheritDoc}
     */
    @Override
    protected Map<String, String> getAllowableValuesImpl()
    {
        final Map<String, String> allowableValues = new LinkedHashMap<>(this.allowedValues.size());

        for (final String allowedValue : this.allowedValues)
        {
            final String displayLabel = this.getI18NLabel(allowedValue);
            allowableValues.put(allowedValue, displayLabel);
        }

        return allowableValues;
    }
}
