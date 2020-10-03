/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.nlpcraft

import java.util.concurrent.Callable
import java.util.function.{BiPredicate, Consumer, Supplier, Function ⇒ JFunction, Predicate ⇒ JPredicate}
import org.apache.nlpcraft.common.ansi.NCAnsi._
import org.apache.nlpcraft.common.util._

import scala.language.implicitConversions

/**
  * Package scope.
  */
package object common {
    // Type aliases for `org.apache.nlpcraft`
    type NCE = NCException
    final val U = NCUtils
    
    // Internal deep debug flag (more verbose tracing).
    final val DEEP_DEBUG = false
    
    // Model and token **internal** metadata keys.
    final val MDL_META_ALL_ELM_IDS_KEY = "__NLPCRAFT_MDL_META_ALL_ELM_IDS"
    final val MDL_META_ALL_GRP_IDS_KEY = "__NLPCRAFT_MDL_META_ALL_GRP_IDS"
    final val MDL_META_ALL_ALIASES_KEY = "__NLPCRAFT_MDL_META_ALL_ALIASES"
    final val TOK_META_ALIASES_KEY = "__NLPCRAFT_TOK_META_ALIASES"

    // Real foreground color shortcuts...
    def i(s: Any): String = s"$ansiReversed${s.toString}$ansiReset"
    def g(s: Any): String = s"$ansiGreenFg${s.toString}$ansiReset"
    def r(s: Any): String = s"$ansiRedFg${s.toString}$ansiReset"
    def c(s: Any): String = s"$ansiCyanFg${s.toString}$ansiReset"
    def y(s: Any): String = s"$ansiYellowFg${s.toString}$ansiReset"
    def w(s: Any): String = s"$ansiWhiteFg${s.toString}$ansiReset"
    def b(s: Any): String = s"$ansiBlueFg${s.toString}$ansiReset"
    def k(s: Any): String = s"$ansiBlackFg${s.toString}$ansiReset"
    
    /**
     * 
     * @param f
     * @tparam T
     * @return
     */
    implicit def toJavaConsumer[T](f: T ⇒ Unit): Consumer[T] = (t: T) => f(t)
    
    /**
      *
      * @param f
      * @tparam A
      * @return
      */
    implicit def toJavaSupplier[A](f: () ⇒ A): Supplier[A] = () => f()
    
    /**
      *
      * @param f
      * @tparam A
      * @tparam B
      * @return
      */
    implicit def toJavaFunction[A, B](f: A ⇒ B): JFunction[A, B] = (a: A) => f(a)
    
    /**
      *
      * @param f
      * @tparam A
      * @return
      */
    implicit def toJavaPredicate[A](f: A ⇒ Boolean): JPredicate[A] = (a: A) => f(a)
    
    /**
      *
      * @param predicate
      * @tparam A
      * @tparam B
      * @return
      */
    implicit def toJavaBiPredicate[A, B](predicate: (A, B) ⇒ Boolean): BiPredicate[A, B] = (a: A, b: B) => predicate(a, b)
    
    /**
      * @param f Lambda to convert.
      * @return Runnable object.
      */
    implicit def toRunnable(f: () ⇒ Unit): Runnable =
        new Runnable() {
            override def run(): Unit = f()
        }
    
    /**
      * @param f Lambda to convert.
      * @return Callable object.
      */
    implicit def toCallable[R](f: () ⇒ R): Callable[R] =
        () => f()
}
