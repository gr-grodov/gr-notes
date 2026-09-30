package gr.grodov.gr_notes.common.logs

import org.slf4j.{Logger, LoggerFactory}

trait Logging {
    protected lazy val logger: Logger = LoggerFactory.getLogger(getClass)
}