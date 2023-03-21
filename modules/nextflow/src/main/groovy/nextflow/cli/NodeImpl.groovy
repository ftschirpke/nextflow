/*
 * Copyright 2013-2023, Seqera Labs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package nextflow.cli

import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import nextflow.config.ConfigBuilder
import nextflow.daemon.DaemonLauncher
import nextflow.plugin.Plugins
import nextflow.util.ServiceName
import nextflow.util.ServiceDiscover

/**
 * CLI `node` sub-command
 *
 * @author Paolo Di Tommaso <paolo.ditommaso@gmail.com>
 */
@Slf4j
@CompileStatic
class NodeImpl {

    interface Options {
        Map<String,String> getClusterOptions()
        String getProvider()

        ILauncherOptions getLauncherOptions()
    }

    @Delegate
    private Options options

    NodeImpl(Options options) {
        this.options = options
    }

    void run() {
        System.setProperty('nxf.node.daemon', 'true')
        launchDaemon(provider)
    }

    /**
     * Launch the daemon service
     *
     * @param config The nextflow configuration map
     */
    protected launchDaemon(String name) {

        // create the config object
        def config = new ConfigBuilder()
            .setLauncherOptions(launcherOptions)
            .setNodeOptions(this)
            .build()

        DaemonLauncher instance
        if( name ) {
            if( name.contains('.') ) {
                instance = loadDaemonByClass(name)
            }
            else {
                instance = loadDaemonByName(name)
            }
        }
        else {
            instance = loadDaemonFirst()
        }

        // launch it
        instance.launch(config)
    }

    /**
     * Load a {@code DaemonLauncher} instance of the its *friendly* name i.e. the name provided
     * by using the {@code ServiceName} annotation on the daemon class definition
     *
     * @param name The executor name e.g. {@code gridgain}
     * @return The daemon launcher instance
     * @throws IllegalStateException if the class does not exist or it cannot be instantiated
     */
    static DaemonLauncher loadDaemonByName( String name ) {

        Class<DaemonLauncher> clazz = null
        for( Class<DaemonLauncher> item : ServiceDiscover.load(DaemonLauncher) ) {
            log.debug "Discovered daemon class: ${item.name}"
            ServiceName annotation = item.getAnnotation(ServiceName)
            if( annotation && annotation.value() == name ) {
                clazz = item
                break
            }
        }

        if( !clazz )
            throw new IllegalStateException("Unknown daemon name: $name")

        try {
            clazz.newInstance()
        }
        catch( Exception e ) {
            throw new IllegalStateException("Unable to launch executor: $name", e)
        }
    }

    /**
     * Load a class implementing the {@code DaemonLauncher} interface by the specified class name
     *
     * @param name The fully qualified class name e.g. {@code nextflow.executor.local.LocalExecutor}
     * @return The daemon launcher instance
     * @throws IllegalStateException if the class does not exist or it cannot be instantiated
     */
    static DaemonLauncher loadDaemonByClass( String name ) {
        try {
            return (DaemonLauncher)Class.forName(name).newInstance()
        }
        catch( Exception e ) {
            throw new IllegalStateException("Cannot load daemon: ${name}")
        }
    }

    /**
     * @return The first available instance of a class implementing {@code DaemonLauncher}
     * @throws IllegalStateException when no class implementing {@code DaemonLauncher} is available
     */
    static DaemonLauncher loadDaemonFirst() {
        Plugins.setup()
        final loader = Plugins.getExtension(DaemonLauncher)
        if( !loader )
            throw new IllegalStateException("No cluster services are available -- Cannot launch Nextflow in cluster mode")

        return loader
    }

}
