#!/usr/bin/env nextflow
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
workflow {
  foo()
}

process foo {

    memory { def x=task.attempt * 1.GB; println "attempt: $task.attempt; memory: $x"; return x }
    time { def x=1.h * task.attempt; println "attempt: $task.attempt; time: $x"; return x }
    errorStrategy { task.exitStatus == 5 && task.attempt<3 ? 'retry' : 'terminate' }
    maxErrors 10
    maxRetries 10

    script:
    """
    exit 5
    """

}
