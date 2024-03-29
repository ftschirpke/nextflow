/*
 * Copyright 2013-2024, Seqera Labs
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

package nextflow.processor

import java.nio.file.Path

import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic
import groovy.transform.Memoized
import groovy.util.logging.Slf4j
import nextflow.exception.MissingFileException
import nextflow.exception.MissingValueException
import nextflow.script.ProcessOutputs
import nextflow.script.ScriptType
import org.codehaus.groovy.runtime.InvokerHelper
/**
 * Implements the resolution of task outputs
 *
 * @author Ben Sherman <bentshermann@gmail.com>
 */
@Slf4j
@CompileStatic
class TaskOutputCollector implements Map<String,?> {

    private ProcessOutputs declaredOutputs

    private boolean optional

    private TaskRun task

    @Delegate
    private Map<String,?> delegate

    TaskOutputCollector(ProcessOutputs declaredOutputs, boolean optional, TaskRun task) {
        this.declaredOutputs = declaredOutputs
        this.optional = optional
        this.task = task
        this.delegate = task.context
    }

    /**
     * Get an environment variable from the task environment.
     *
     * @param name
     */
    String env(String name) {
        final result = env0(task.workDir).get(name)

        if( result == null && !optional )
            throw new MissingValueException("Missing environment variable: $name")

        return result
    }

    /**
     * Get the result of an eval command from the task environment.
     *
     * @param name
     */
    String eval(String name) {
        final evalCmds = task.getOutputEvals()
        final result = env0(task.workDir, evalCmds).get(name)

        if( result == null && !optional )
            throw new MissingValueException("Missing result of eval command: '${evalCmds.get(name)}'")

        return result
    }

    @Memoized(maxCacheSize = 10_000)
    static private Map env0(Path workDir, Map<String,String> evalCmds=null) {
        new TaskEnvCollector(workDir, evalCmds).collect()
    }

    /**
     * Get a file or list of files from the task environment.
     *
     * @param key
     */
    Object path(String key) {
        final param = declaredOutputs.getFiles().get(key)
        final result = new TaskFileCollecter(param, task).collect()

        if( result instanceof Path )
            task.outputFiles.add(result)
        else if( result instanceof Collection<Path> )
            task.outputFiles.addAll(result)

        return result
    }

    /**
     * Get the standard output from the task environment.
     */
    Object stdout() {
        final value = task.@stdout

        if( value == null && task.type == ScriptType.SCRIPTLET )
            throw new IllegalArgumentException("Missing 'stdout' for process > ${task.lazyName()}")

        if( value instanceof Path && !value.exists() )
            throw new MissingFileException("Missing 'stdout' file: ${value.toUriString()} for process > ${task.lazyName()}")

        return value instanceof Path ? ((Path)value).text : value?.toString()
    }

    /**
     * Get a variable from the task context.
     *
     * @param name
     */
    @Override
    @CompileDynamic
    Object get(Object name) {
        if( name == 'stdout' )
            return stdout()

        try {
            return InvokerHelper.getProperty(delegate, name)
        }
        catch( MissingPropertyException e ) {
            throw new MissingValueException("Missing variable in process output: ${e.property}")
        }
    }
}