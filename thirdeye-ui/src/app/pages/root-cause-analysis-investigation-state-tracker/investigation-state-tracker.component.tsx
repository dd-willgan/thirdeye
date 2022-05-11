import { Box, Card, CardContent, Link, Typography } from "@material-ui/core";
import { cloneDeep, isEmpty } from "lodash";
import React, { FunctionComponent, useEffect, useState } from "react";
import { useTranslation } from "react-i18next";
import { Outlet, useParams, useSearchParams } from "react-router-dom";
import { InvestigationOptions } from "../../components/rca/investigation-options/investigation-options.component";
import {
    AppLoadingIndicatorV1,
    HelpLinkIconV1,
    NotificationTypeV1,
    PageHeaderActionsV1,
    PageHeaderTextV1,
    PageHeaderV1,
    PageV1,
    TooltipV1,
    useNotificationProviderV1,
} from "../../platform/components";
import { ActionStatus } from "../../rest/actions.interfaces";
import { Investigation, SavedStateKeys } from "../../rest/dto/rca.interfaces";
import { useGetInvestigation } from "../../rest/rca/rca.actions";
import {
    createNewInvestigation,
    determineInvestigationIDFromSearchParams,
    INVESTIGATION_ID_QUERY_PARAM,
} from "../../utils/investigation/investigation.util";
import { getAnomaliesAnomalyViewPath } from "../../utils/routes/routes.util";
import { RootCauseAnalysisForAnomalyPageParams } from "../root-cause-analysis-for-anomaly-page/root-cause-analysis-for-anomaly-page.interfaces";

export const InvestigationStateTracker: FunctionComponent = () => {
    const { t } = useTranslation();
    const { notify } = useNotificationProviderV1();
    const [searchParams, setSearchParams] = useSearchParams();
    const [investigationId, setInvestigationId] = useState(
        determineInvestigationIDFromSearchParams(searchParams)
    );
    const [localInvestigation, setLocalInvestigation] =
        useState<Investigation | null>(null);
    const [investigationFromServer, setInvestigationFromServer] =
        useState<Investigation | null>(null);
    const { id: anomalyId } =
        useParams<RootCauseAnalysisForAnomalyPageParams>();

    const {
        getInvestigation,
        status: getInvestigationRequestStatus,
        errorMessages: getInvestigationRequestErrors,
    } = useGetInvestigation();

    /**
     * If investigation's associated anomaly id does not match current anomalyId
     * then remove it from the search params to effectively disassociate it from
     * the UI
     */
    useEffect(() => {
        if (
            investigationFromServer &&
            investigationFromServer.anomaly &&
            investigationFromServer.anomaly.id
        ) {
            if (Number(anomalyId) !== investigationFromServer.anomaly.id) {
                searchParams.delete(INVESTIGATION_ID_QUERY_PARAM);
                setSearchParams(searchParams);
            }
        }
    }, [anomalyId, investigationFromServer]);

    useEffect(() => {
        setInvestigationId(
            determineInvestigationIDFromSearchParams(searchParams)
        );
        if (localInvestigation) {
            localInvestigation.uiMetadata[SavedStateKeys.QUERY_SEARCH_STRING] =
                searchParams.toString();
            handleInvestigationChange(localInvestigation);
        }
    }, [searchParams]);

    /**
     * Inform users there was issues when retrieving the investigation for the
     * associated investigation id
     */
    useEffect(() => {
        if (getInvestigationRequestStatus === ActionStatus.Error) {
            const genericMsg = t("message.error-while-fetching", {
                entity: t("label.saved-investigation"),
            });
            !isEmpty(getInvestigationRequestErrors)
                ? getInvestigationRequestErrors.map((msg) =>
                      notify(NotificationTypeV1.Error, `${genericMsg}: ${msg}`)
                  )
                : notify(NotificationTypeV1.Error, genericMsg);
        }
    }, [getInvestigationRequestStatus, getInvestigationRequestErrors]);

    useEffect(() => {
        if (investigationId) {
            getInvestigation(investigationId).then(
                (fetchedInvestigation: Investigation | undefined) => {
                    if (fetchedInvestigation) {
                        setInvestigationFromServer(fetchedInvestigation);
                        setLocalInvestigation(
                            cloneDeep<Investigation>(fetchedInvestigation)
                        );
                    } else {
                        setLocalInvestigation(
                            createNewInvestigation(Number(anomalyId))
                        );
                    }
                }
            );
        } else {
            setLocalInvestigation(createNewInvestigation(Number(anomalyId)));
            setInvestigationFromServer(null);
        }
    }, [investigationId]);

    // Ensure localInvestigation is always so the children views can access a valid object
    if (
        localInvestigation === null ||
        getInvestigationRequestStatus === ActionStatus.Working
    ) {
        return (
            <PageV1>
                <Card variant="outlined">
                    <CardContent>
                        <Box padding={20}>
                            <AppLoadingIndicatorV1 />
                        </Box>
                    </CardContent>
                </Card>
            </PageV1>
        );
    }

    const handleInvestigationChange = (
        modifiedInvestigation: Investigation
    ): void => {
        setLocalInvestigation(cloneDeep(modifiedInvestigation));
    };

    const handleServerUpdatedInvestigation = (
        updatedInvestigation: Investigation
    ): void => {
        searchParams.set(
            INVESTIGATION_ID_QUERY_PARAM,
            updatedInvestigation.id.toString()
        );
        setSearchParams(searchParams, { replace: true });
        setInvestigationFromServer(updatedInvestigation);
        setLocalInvestigation(cloneDeep(updatedInvestigation));
    };

    const handleRemoveInvestigationAssociation = (): void => {
        searchParams.delete(INVESTIGATION_ID_QUERY_PARAM);
        setSearchParams(searchParams, { replace: true });
        setInvestigationFromServer(null);
        setLocalInvestigation(createNewInvestigation(Number(anomalyId)));
    };

    return (
        <PageV1>
            <PageHeaderV1>
                <Box display="inline">
                    <div>
                        <PageHeaderTextV1>
                            {t("label.root-cause-analysis")}:{" "}
                            {t("label.anomaly")}{" "}
                            <Link
                                href={getAnomaliesAnomalyViewPath(
                                    Number(anomalyId)
                                )}
                            >
                                #{anomalyId}
                            </Link>
                            <TooltipV1
                                placement="top"
                                title={
                                    t(
                                        "label.how-to-perform-root-cause-analysis-doc"
                                    ) as string
                                }
                            >
                                <span>
                                    <HelpLinkIconV1
                                        displayInline
                                        enablePadding
                                        externalLink
                                        href="https://dev.startree.ai/docs/thirdeye/how-tos/perform-root-cause-analysis"
                                    />
                                </span>
                            </TooltipV1>
                        </PageHeaderTextV1>
                    </div>
                    {investigationId && (
                        <div>
                            <Typography variant="subtitle1">
                                Viewing saved investigation (#
                                {investigationId}):{" "}
                                {investigationFromServer &&
                                    investigationFromServer.name}
                            </Typography>
                        </div>
                    )}
                </Box>

                <PageHeaderActionsV1>
                    <InvestigationOptions
                        investigationId={investigationId}
                        localInvestigation={localInvestigation}
                        serverInvestigation={investigationFromServer}
                        onRemoveInvestigationAssociation={
                            handleRemoveInvestigationAssociation
                        }
                        onSuccessfulUpdate={handleServerUpdatedInvestigation}
                    />
                </PageHeaderActionsV1>
            </PageHeaderV1>

            <Outlet
                context={{
                    investigation: localInvestigation,
                    investigationHasChanged: handleInvestigationChange,
                }}
            />
        </PageV1>
    );
};
