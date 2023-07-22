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

import com.beust.jcommander.Parameter
import com.beust.jcommander.Parameters
import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import nextflow.exception.AbortOperationException
import nextflow.plugin.Plugins
import nextflow.scm.AssetManager

/**
 * CLI sub-command VIEW -- Print a pipeline script to console
 *
 * @author Paolo Di Tommaso <paolo.ditommaso@gmail.com>
 */
@Slf4j
@CompileStatic
class CmdView {

    static final public NAME = 'view'

    interface Options {
        String getPipeline()
        boolean getQuiet()
        boolean getAll()
    }

    @Parameters(commandDescription = "View project script file(s)")
    static class V1 extends CmdBase implements Options {

        @Override
        String getName() { NAME }

        @Parameter(description = 'project name', required = true)
        List<String> args = []

        @Parameter(names = '-q', description = 'Hide header line', arity = 0)
        boolean quiet

        @Parameter(names = '-l', description = 'List repository content', arity = 0)
        boolean all

        @Override
        String getPipeline() {
            args.size() > 0 ? args[0] : null
        }

        @Override
        void run() {
            new CmdView(this).run()
        }
    }

    @Delegate
    private Options options

    CmdView(Options options) {
        this.options = options
    }

    void run() {
        Plugins.init()
        def manager = new AssetManager(pipeline)
        if( !manager.isLocal() )
            throw new AbortOperationException("Unknown project name `${pipeline}`")

        if( all ) {
            if( !quiet )
                println "== content of path: ${manager.localPath}"

            manager.localPath.eachFile { File it ->
                println it.name
            }
        }

        else {
            /*
             * prints the script main file
             */
            final script = manager.getMainScriptFile()
            if( !script.exists() )
                throw new AbortOperationException("Missing script file: '${script}'")

            if( !quiet )
                println "== content of file: $script"

            script.readLines().each { println it }
        }

    }
}
