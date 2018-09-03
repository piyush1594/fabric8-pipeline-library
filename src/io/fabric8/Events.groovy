package io.fabric8

class Events implements Serializable {
    enum EVENT {
        PIPELINE_START,
        PIPELINE_END
    }

    static private listeners = [:]

    static def on(String event, Closure c) {
        on([event], c)
    }

    static def on(List events, Closure c) {
        events.each { e ->
            listeners[e] = listeners[e] ?: [] as Set
            listeners[e].add(c)
            println "... registered for $e ${listeners[e]}"
        }
    }

    static def emit(String event, Object... args) {
        emit([event], args)
    }

    static def emit(List events, Object... args) {
        events.each { e ->
            if (!listeners[e]) {
                return
            }
            listeners[e].each { c -> c.call([name: e], args) }
        }
    }

}
