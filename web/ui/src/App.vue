<template>
    <div id="app">
        <terminal/>
    </div>
</template>

<script>
import Terminal from './terminal/Terminal'

export default {
    name: 'app',
    components: { Terminal }
}
</script>

<style>
    #app {
        font-family: 'Avenir', Helvetica, Arial, sans-serif;
        -webkit-font-smoothing: antialiased;
        -moz-osx-font-smoothing: grayscale;
        text-align: center;
        color: #2c3e50;
        margin-top: 60px;
    }
</style>
