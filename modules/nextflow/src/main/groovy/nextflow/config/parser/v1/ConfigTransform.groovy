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

package nextflow.config.parser.v1

import java.lang.annotation.ElementType
import java.lang.annotation.Retention
import java.lang.annotation.RetentionPolicy
import java.lang.annotation.Target

import org.codehaus.groovy.transform.GroovyASTTransformationClass
/**
 * Nextflow configuration file AST xform marker interface
 *
 * @author Paolo Di Tommaso <paolo.ditommaso@gmail.com>
 */
@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.METHOD)
@GroovyASTTransformationClass(classes = [ConfigTransformImpl])
@interface ConfigTransform {
    /**
     * hack to pass a parameter in the {@link ConfigTransformImpl} class -- do not remove
     *
     * See {@link ConfigTransformImpl#renderClosureAsString}
     */
    boolean renderClosureAsString()
}
