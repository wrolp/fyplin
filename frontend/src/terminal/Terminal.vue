<template>
    <div id="terminal"></div>
</template>

<script>
    import {Terminal} from 'xterm'

    export default {
        name: "terminal",
        mounted () {
            let container = document.getElementById('terminal')
        }
    }
</script>

<style scoped>

</style>
