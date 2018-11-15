<template>
    <div id="terminal" class="console"></div>
</template>

<script>
import { Terminal } from 'xterm'
import { fit } from 'xterm/lib/addons/fit/fit'
// eslint-disable-next-line
import * as attach from 'xterm/lib/addons/attach/attach'

export default {
    name: 'terminal',
    data () {
        return {
            xterm: null,
            ws: null
        }
    },
    mounted () {
        Terminal.applyAddon(attach)
        let container = document.getElementById('terminal')
        this.xterm = new Terminal()
        this.xterm.open(container)
        fit(this.xterm)

        let uri = 'ws://' + window.location.host + '/ws'
        this.ws = new WebSocket(uri)
        this.ws.onopen = this.runTerminal
        this.ws.onclose = () => {
            this.xterm.destroy()
        }
        this.ws.onerror = () => {}
    },
    methods: {
        runTerminal () {
            this.xterm.attach(this.ws)
            this.xterm._initialized = true
        }
    },
    beforeDestroy () {
        if (this.ws) {
            this.ws.close()
        }
        if (this.xterm) {
            this.xterm.destroy()
        }
    }
}
</script>

<style scoped>

</style>
