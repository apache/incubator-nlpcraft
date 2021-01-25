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

package org.apache.nlpcraft.examples.sql

import org.apache.nlpcraft.examples.sql.db.SqlServer
import org.apache.nlpcraft.model.tools.sqlgen.impl.NCSqlModelGeneratorImpl
import org.junit.jupiter.api.{AfterEach, BeforeEach, Test}

import java.io.File

/**
  * SQL generator smoke test, based on H2 example database.
  *
  * @see SqlModel
  */
class NCSqlGeneratorSpec {
    private final val PATH = "sql.gen.out.yaml"

    private var sqlStarted = false

    @BeforeEach
    def start(): Unit = {
        SqlServer.start()

        sqlStarted = true
    }

    @AfterEach
    def stop(): Unit = {
        sqlStarted = false

        val f = new File(PATH)

        if (f.exists()) {
            if (f.delete())
                println(s"Test output deleted: ${f.getAbsolutePath}")
            else
                System.err.println(s"Couldn't delete file: ${f.getAbsolutePath}")
        }

        if (sqlStarted)
            SqlServer.stop()
    }

    @Test
    def test(): Unit =
        NCSqlModelGeneratorImpl.process(
            Array(
                s"--url=${SqlServer.H2_URL}",
                s"--driver=org.h2.Driver",
                s"--schema=PUBLIC",
                s"--out=$PATH"
            )
        )
}
