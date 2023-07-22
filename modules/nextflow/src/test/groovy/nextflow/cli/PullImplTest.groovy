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

import java.nio.file.Files

import nextflow.plugin.Plugins
import spock.lang.IgnoreIf
import spock.lang.Requires
import spock.lang.Specification
/**
 *
 * @author Paolo Di Tommaso <paolo.ditommaso@gmail.com>
 */
@IgnoreIf({System.getenv('NXF_SMOKE')})
class PullImplTest extends Specification {

    def cleanup() {
        Plugins.stop()
    }
    
    @Requires({ System.getenv('NXF_GITHUB_ACCESS_TOKEN') })
    def 'should pull the github repository in the local folder'() {

        given:
        def accessToken = System.getenv('NXF_GITHUB_ACCESS_TOKEN')
        def dir = Files.createTempDirectory('test')
        def options = Mock(CmdPull.Options) {
            pipeline >> 'nextflow-io/hello'
            hubUser >> accessToken
        }
        def cmd = new CmdPull(options: options, root: dir.toFile())

        when:
        cmd.run()
        then:
        dir.resolve('nextflow-io/hello/.git').exists()
        dir.resolve('nextflow-io/hello/README.md').exists()

        cleanup:
        dir?.deleteDir()
        Plugins.stop()
    }

}
