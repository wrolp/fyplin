<template>
    <div id="terminal" class="console"></div>
</template>

<script>
import { Terminal } from 'xterm'

export default {
    name: 'terminal',
    data () {
        return {
            xterm: null,
            ws: null
        }
    },
    mounted () {
        let container = document.getElementById('terminal')
        this.xterm = new Terminal()
        this.xterm.open(container)

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
        this.ws.close()
        this.xterm.destroy()
    }
}
</script>

<style scoped>

</style>
