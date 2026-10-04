$ErrorActionPreference = 'Stop'
$project = Split-Path $PSScriptRoot -Parent
$jdk = 'C:/Program Files/Java/jdk-21.0.10/bin'
$classes = Join-Path $project 'build/effect-validation'
New-Item -ItemType Directory -Force $classes | Out-Null
$jars = foreach ($module in @('lwjgl','lwjgl-glfw','lwjgl-opengl')) {
    $base = Join-Path $env:USERPROFILE ".gradle/caches/modules-2/files-2.1/org.lwjgl/$module/3.3.3"
    Get-ChildItem $base -Recurse -Filter '*.jar' | Where-Object { $_.Name -eq "$module-3.3.3.jar" -or $_.Name -eq "$module-3.3.3-natives-windows.jar" } | Select-Object -ExpandProperty FullName
}
$classpath = ($jars + $classes) -join ';'
& "$jdk/javac.exe" -encoding UTF-8 -cp $classpath -d $classes "$PSScriptRoot/ShaderSmokeTest.java" "$PSScriptRoot/AuctionPriceTest.java" "$project/src/main/java/socket/util/AuctionPrice.java"
if ($LASTEXITCODE -ne 0) { throw 'Validation compilation failed' }
& "$jdk/java.exe" -cp $classpath AuctionPriceTest
if ($LASTEXITCODE -ne 0) { throw 'Price tests failed' }
& "$jdk/java.exe" -cp $classpath ShaderSmokeTest "$project/src/main/resources/assets/socket/shaders/core"
if ($LASTEXITCODE -ne 0) { throw 'Shader tests failed' }
