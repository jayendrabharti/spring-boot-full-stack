:: Created by frontend-maven-plugin, please don't edit manually.
@ECHO OFF

SETLOCAL

SET "NODE_EXE=%~dp0\node.exe"
SET "PNPM_CLI_JS=%~dp0\node_modules\pnpm\bin\pnpm.cjs"

"%NODE_EXE%" "%PNPM_CLI_JS%" %*