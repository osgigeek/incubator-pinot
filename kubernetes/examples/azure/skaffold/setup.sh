#!/usr/bin/env bash
#
# Licensed to the Apache Software Foundation (ASF) under one
# or more contributor license agreements.  See the NOTICE file
# distributed with this work for additional information
# regarding copyright ownership.  The ASF licenses this file
# to you under the Apache License, Version 2.0 (the
# "License"); you may not use this file except in compliance
# with the License.  You may obtain a copy of the License at
#
#   http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing,
# software distributed under the License is distributed on an
# "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
# KIND, either express or implied.  See the License for the
# specific language governing permissions and limitations
# under the License.
#

set -e
if [[ -z "${AZURE_RESOURCE_GROUP}" ]] || [[ -z "${AZURE_USER}" ]]
then
  echo "Please set both \$AZURE_RESOURCE_GROUP variables. E.g. AZURE_USER=<<email>> AZURE_RESOURCE_GROUP=<<resource-group-name>> ./setup.sh"
  exit 1
fi

AZURE_CLUSTER=sanayak-pinot
AZURE_NODE_POOL=pinotpool
AZURE_MACHINE_TYPE=Standard_D2_v3
AZURE_NUM_NODES=1
AZURE_ZONE=1
AZURE_LOAD_BALANCER=standard

az aks create --resource-group ${AZURE_RESOURCE_GROUP} --name ${AZURE_CLUSTER} \
--node-count ${AZURE_NUM_NODES} \
--node-vm-size ${AZURE_MACHINE_TYPE} \
--generate-ssh-keys \
--load-balancer-sku ${AZURE_LOAD_BALANCER}

az aks get-credentials --name ${AZURE_CLUSTER} --resource-group ${AZURE_RESOURCE_GROUP}

AZURE_MACHINE_TYPE=Standard_D4_v3

az aks nodepool add  --name ${AZURE_NODE_POOL} \
--resource-group ${AZURE_RESOURCE_GROUP} \
--cluster ${AZURE_CLUSTER} \
--node-vm-size ${AZURE_MACHINE_TYPE} \
--node-count ${AZURE_NUM_NODES}

kubectl create namespace ${AZURE_CLUSTER}

kubectl create clusterrolebinding cluster-admin-binding --clusterrole=cluster-admin --user ${AZURE_USER}
kubectl create clusterrolebinding add-on-cluster-admin --clusterrole=cluster-admin --serviceaccount=sanayak-pinot:default
