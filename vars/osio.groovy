#!/usr/bin/groovy

import io.fabric8.Events
import io.fabric8.plugins.*

def call(body) {
    node {
        // TODO: move registration to a different file; perhaps
        // plugins.register()?
        // new analytics().register()
        Events.emit("pipeline.start", "testarg")
        checkout scm
        spawn(image: "oc") { body() }
        Events.emit("pipeline.end", "testarg")
    }
}
