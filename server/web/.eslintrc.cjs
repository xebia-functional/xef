module.exports = {
  env: { browser: true, es2020: true },
  extends: [
    'eslint:recommended',
    'plugin:@typescript-eslint/recommended',
    'plugin:react-hooks/recommended',
    'prettier',
    'plugin:import/recommended',
    'plugin:import/typescript',
  ],
  parser: '@typescript-eslint/parser',
  parserOptions: { ecmaVersion: 'latest', sourceType: 'module' },
  plugins: ['@typescript-eslint', 'prettier', 'react-refresh', 'import'],
  rules: {
    'prettier/prettier': 'error',
    'import/no-unresolved': ['error', { ignore: ['^@/'] }],
    'import/order': [
      'error',
      {
        groups: [
          'builtin',
          'external',
          'internal',
          'parent',
          'sibling',
          'index',
          'object',
          'type',
        ],
        pathGroups: [
          {
            pattern: '@/components/**',
            group: 'internal',
            position: 'after',
          },
          {
            pattern: '@/state/**',
            group: 'internal',
            position: 'after',
          },
          {
            pattern: '@/utils/**',
            group: 'internal',
            position: 'after',
          },
          {
            pattern: '@/styles/**',
            group: 'internal',
            position: 'after',
          },
          {
            pattern: '@/assets/**',
            group: 'internal',
            position: 'after',
          },
        ],
        distinctGroup: true,
        pathGroupsExcludedImportTypes: ['@/**'],
        warnOnUnassignedImports: true,
        'newlines-between': 'always',
      },
    ],
    '@typescript-eslint/no-unused-vars': [
      'error',
      { argsIgnorePattern: '^_', varsIgnorePattern: '^_' },
    ],
  },
  settings: {
    'import/parsers': {
      '@typescript-eslint/parser': ['.ts', '.tsx'],
    },
    'import/resolver': {
      typescript: {
        alwaysTryTypes: true,
      },
    },
  },
};
