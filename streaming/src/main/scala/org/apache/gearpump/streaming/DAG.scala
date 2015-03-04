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

package org.apache.gearpump.streaming

import org.apache.gearpump.partitioner.Partitioner
import org.apache.gearpump.streaming.task.TaskId
import org.apache.gearpump.util.Graph
import upickle._

import scala.collection.JavaConversions._


case class DAG(tasks : Map[ProcessorId, TaskDescription], graph : Graph[ProcessorId, Partitioner]) extends Serializable {

  def subGraph(processorId : Int): DAG = {
    val newGraph = Graph.empty[ProcessorId, Partitioner]
    newGraph.addVertex(processorId)
    graph.edgesOf(processorId).foreach { edge =>
      val (node1, partitioner, node2) = edge
      newGraph.addEdge(node1, partitioner, node2)
    }
    val newMap = newGraph.vertices.foldLeft(Map.empty[ProcessorId, TaskDescription]){ (map, vertex) =>
      val task = tasks.get(vertex).get

      //clean out other in-degree and out-degree tasks' data except the task parallelism
      map + (vertex -> task.copy(null, task.parallelism))
    }
    new DAG(newMap, newGraph)
  }

  def taskCount: Int = {
    tasks.foldLeft(0) { (count, task) =>
      count + task._2.parallelism
    }
  }
}

object DAG {

  implicit def graphToDAG(graph: Graph[TaskDescription, Partitioner]): DAG = {
    apply(graph)
  }

  def apply (graph : Graph[TaskDescription, Partitioner]) : DAG = {
    val topologicalOrderIterator = graph.topologicalOrderIterator

    val outputGraph = Graph.empty[ProcessorId, Partitioner]
    val (_, taskMap) = topologicalOrderIterator.foldLeft((0, Map.empty[ProcessorId, TaskDescription])) { (first, task) =>
      val (processorId, tasks) = first
      outputGraph.addVertex(processorId)
      (processorId + 1, tasks + (processorId -> task))
    }

    graph.edges.foreach { edge =>
      val (node1, partitioner, node2) = edge
      outputGraph.addEdge(getProcessorId(taskMap, node1), partitioner, getProcessorId(taskMap, node2))
    }

    new DAG(taskMap, outputGraph)
  }

  def empty() = apply(Graph.empty)

  private def getProcessorId(tasks : Map[ProcessorId, TaskDescription], node : TaskDescription) = {
    tasks.find { task =>
      val (_, taskDescription) = task
      taskDescription.equals(node)
    }.get._1
  }
}