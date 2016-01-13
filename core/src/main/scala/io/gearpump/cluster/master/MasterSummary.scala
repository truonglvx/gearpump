/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.gearpump.cluster.master

import io.gearpump.util.HistoryMetricsService.HistoryMetricsConfig

object MasterStatus {
  type Type = String
  val Synced = "synced"
  val UnSynced = "unsynced"
}

/**
 * Master information for REST API call
 */
case class MasterSummary(
  leader: (String, Int),
  cluster: List[(String, Int)],
  aliveFor: Long,
  logFile: String,
  jarStore: String,
  masterStatus: MasterStatus.Type,
  homeDirectory: String,
  activities: List[MasterActivity],
  jvmName: String,
  historyMetricsConfig: HistoryMetricsConfig = null)

case class MasterActivity(time: Long, event: String)