/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import org.apache.tinkerpop.gremlin.server.GremlinServer
import org.apache.tinkerpop.gremlin.server.Settings
import org.apache.tinkerpop.gremlin.server.Settings.SerializerSettings

if (Boolean.parseBoolean(skipTests)) return

log.info("Starting Gremlin Server instances for native testing of ${executionName}")
def settings = Settings.read("${settingsFile}")
settings.graphs.graph = gremlinServerDir + "/conf/tinkergraph-empty.properties"
settings.scriptEngines["gremlin-groovy"].plugins["org.apache.tinkerpop.gremlin.jsr223.ScriptFileGremlinPlugin"].files = [gremlinServerDir + "/scripts/generate-modern.groovy"]
settings.serializers << new SerializerSettings("org.apache.tinkerpop.gremlin.driver.ser.GraphSONMessageSerializerGremlinV2d0", [:])
settings.port = 45940

def server = new GremlinServer(settings)
server.start().join()

project.setContextValue("gremlin.server", server)
log.info("Gremlin Server with no authentication started on port 45940")

def securePropsFile = new File("${projectBaseDir}/target/tinkergraph-credentials.properties")
if (!securePropsFile.exists()) {
    securePropsFile.createNewFile()
    securePropsFile << "gremlin.graph=org.apache.tinkerpop.gremlin.tinkergraph.structure.TinkerGraph\n"
    securePropsFile << "gremlin.tinkergraph.vertexIdManager=LONG\n"
    securePropsFile << "gremlin.tinkergraph.graphLocation=${gremlinServerDir}/data/credentials.kryo\n"
    securePropsFile << "gremlin.tinkergraph.graphFormat=gryo"
}

def settingsSecure = Settings.read("${settingsFile}")
settingsSecure.graphs.graph = gremlinServerDir + "/conf/tinkergraph-empty.properties"
settingsSecure.scriptEngines["gremlin-groovy"].plugins["org.apache.tinkerpop.gremlin.jsr223.ScriptFileGremlinPlugin"].files = [gremlinServerDir + "/scripts/generate-modern.groovy"]
settingsSecure.serializers << new SerializerSettings("org.apache.tinkerpop.gremlin.driver.ser.GraphSONMessageSerializerGremlinV2d0", [:])
settingsSecure.port = 45941
settingsSecure.authentication.authenticator = "org.apache.tinkerpop.gremlin.server.auth.SimpleAuthenticator"
settingsSecure.authentication.config = [credentialsDb: projectBaseDir + "/target/tinkergraph-credentials.properties"]

def serverSecure = new GremlinServer(settingsSecure)
serverSecure.start().join()

project.setContextValue("gremlin.server.secure", serverSecure)
log.info("Gremlin Server with authentication started on port 45941")