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

(function()
{
    var Dom, $html;

    Dom = YAHOO.util.Dom;
    $html = Alfresco.util.encodeHTML;

    if (Alfresco.RuleConfigAction)
    {
        Alfresco.RuleConfigAction.prototype.customisations['acosix-actions.webhookCall'] = {
            itemType : 'action',
            edit : function acosix_actions_RCA_webhookCall(configDef)
            {
                // custom UI, so hide default
                this._hideParameters(configDef.parameterDefinitions);

                configDef.parameterDefinitions.push({
                    type : 'acosix-actions.webhookCall.configDialogButton',
                    _buttonLabel : this.msg('button.options')
                });
                return configDef;
            }
        };

        Alfresco.RuleConfigAction.prototype.renderers['acosix-actions.webhookCall.configDialogButton'] = {
            manual : {
                edit : true
            },
            currentCtx : {},
            edit : function acosix_actions_RCA_webhookCall_configDialogButton(containerEl, configDef, paramDef, ruleConfig)
            {
                this
                        ._createButton(
                                containerEl,
                                configDef,
                                paramDef,
                                ruleConfig,
                                function acosix_actions_RCA_webhookCall_configDialog__onClick(type, obj)
                                {
                                    var scope, templateUrl;

                                    this.renderers['acosix-actions.webhookCall.configDialogButton'].currentCtx = {
                                        configDef : obj.configDef,
                                        ruleConfig : obj.ruleConfig
                                    };

                                    if (!this.widgets.acosixActionsWebhookConfigForm)
                                    {
                                        this.widgets.acosixActionsWebhookConfigForm = new Alfresco.module.SimpleDialog(this.id
                                                + '-webhookCall.configDialog-' + Alfresco.util.generateDomId());

                                        templateUrl = YAHOO.lang
                                                .substitute(
                                                        Alfresco.constants.URL_SERVICECONTEXT
                                                                + 'components/form?itemKind={itemKind}&itemId={itemId}&mode={mode}&submitType={submitType}&showCancelButton=true',
                                                        {
                                                            itemKind : 'action',
                                                            itemId : 'acosix-actions.webhookCall',
                                                            mode : 'edit',
                                                            submitType : 'json'
                                                        });

                                        scope = this;
                                        this.widgets.acosixActionsWebhookConfigForm.setOptions({
                                            width : 'auto',
                                            templateUrl : templateUrl,
                                            actionUrl : null,
                                            destroyOnHide : false,
                                            doBeforeDialogShow : {
                                                fn : function acosix_actions_RCA_webhookCall_configDialog__beforeDialogShow(form, dialog)
                                                {
                                                    var ctx, params, props, idx;

                                                    Alfresco.util.populateHTML([ dialog.id + '-dialogTitle',
                                                            $html(scope.msg('button.options')) ]);

                                                    ctx = scope.renderers['acosix-actions.webhookCall.configDialogButton'].currentCtx;
                                                    params = scope._getParameters(ctx.configDef);
                                                    props = [ 'urlTemplate', 'urlTemplateArguments', 'payloadTemplate',
                                                            'payloadTemplateArguments', 'payloadMimetype', 'payloadMimetype', 'headers' ];

                                                    for (idx = 0; idx < props.length; idx++)
                                                    {
                                                        Dom.get(this.id + '_prop_' + props[idx]).value = params[props[idx]] || '';
                                                    }
                                                },
                                                scope : this.widgets.acosixActionsWebhookConfigForm
                                            },
                                            doBeforeAjaxRequest : {
                                                fn : function acosix_actions_RCA_webhookCall_configDialog__beforeAjaxRequest(config)
                                                {
                                                    var ctx, props, idx;

                                                    ctx = scope.renderers['acosix-actions.webhookCall.configDialogButton'].currentCtx;
                                                    props = [ 'urlTemplate', 'urlTemplateArguments', 'payloadTemplate',
                                                            'payloadTemplateArguments', 'payloadMimetype', 'payloadMimetype', 'headers' ];

                                                    for (idx = 0; idx < props.length; idx++)
                                                    {
                                                        scope._setHiddenParameter(ctx.configDef, ctx.ruleConfig, props[idx],
                                                                config.dataObj['prop_' + props[idx]] || null);
                                                    }

                                                    scope._updateSubmitElements(ctx.configDef);

                                                    this.widgets.cancelButton.set('disabled', false);
                                                    scope.widgets.acosixActionsWebhookConfigForm.hide();
                                                    return false;
                                                },
                                                scope : this.widgets.acosixActionsWebhookConfigForm
                                            }
                                        });
                                    }

                                    this.widgets.acosixActionsWebhookConfigForm.show();
                                });
            }
        };
    }
}());
