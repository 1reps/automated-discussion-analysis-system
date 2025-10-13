import pluginVue from 'eslint-plugin-vue';
import prettier from 'eslint-config-prettier';

export default [
  { ignores: ['dist', 'node_modules'] },
  ...pluginVue.configs['flat/recommended'],
  prettier,
  {
    rules: {
      'padding-line-between-statements': [
        'error',
        { blankLine: 'always', prev: 'function', next: '*' },
        { blankLine: 'always', prev: '*', next: 'function' }
      ]
    }
  }
];
