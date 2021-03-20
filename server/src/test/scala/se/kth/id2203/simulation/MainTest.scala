package se.kth.id2203.simulation

import org.scalatest.Suites


class MainTest extends Suites (new OperationTest, new LINTest, new ServerCrashTest)