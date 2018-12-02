module.exports = {
    root: true,
    'extends': [
        'plugin:vue/essential',
        '@vue/standard'
    ],
    rules: {
        'indent': ['error', 4],
        'generator-star-spacing': 0,
        'no-debugger': process.env.NODE_ENV === 'production' ? 'error' : 'off',
        // 'vue/no-parsing-error': [2, { 'x-invalid-end-tag': false }],
        'no-undef': 'off',
        'no-multiple-empty-lines': 0,
        'no-mixed-operators': 0,
        'arrow-parens': 0,
        'linebreak-style': 2,
        'no-var': 2
    },
    parserOptions: {
        parser: 'babel-eslint'
    }
}
