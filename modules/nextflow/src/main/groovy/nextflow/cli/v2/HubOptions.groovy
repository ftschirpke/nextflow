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

package nextflow.cli.v2

import groovy.transform.CompileStatic
import nextflow.cli.IHubOptions
import picocli.CommandLine.Option

/**
 * CLI v2 implementation of command line options related to interacting with
 * a git registry (GitHub, BitBucket, etc)
 *
 * @author Ben Sherman <bentshermann@gmail.com>
 */
@CompileStatic
trait HubOptions implements IHubOptions {

    @Option(names = ['--hub'], paramLabel = '<hub>', description = 'Service hub where the project is hosted')
    String hubProvider

    @Option(names = ['--user'], paramLabel = '<user>', description = 'Private repository user name')
    String hubUserCli

}
