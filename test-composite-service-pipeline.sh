#!/bin/bash
# test-composite-service-pipeline.sh
# Script pour tester localement les étapes du pipeline CI/CD du composite-service

set -e  # Exit on error

echo "================================"
echo "Testing Composite Service Pipeline"
echo "================================"

# Variables (similaires au GitLab CI)
export MAVEN_OPTS="-Dmaven.repo.local=.m2/repository"
export MAVEN_CLI_OPTS="--batch-mode --errors --fail-at-end --show-version"

cd "$(dirname "$0")/services/composite-service"

echo ""
echo "Step 1/3: Building composite-service..."
echo "========================================"
mvn $MAVEN_CLI_OPTS clean package -DskipTests
echo "✅ Build successful!"

echo ""
echo "Step 2/3: Running tests..."
echo "========================================"
mvn $MAVEN_CLI_OPTS test
echo "✅ Tests passed!"

echo ""
echo "Step 3/3: Building Docker image..."
echo "========================================"
docker build -t willbank-composite-service:test .
echo "✅ Docker image built!"

echo ""
echo "================================"
echo "✅ All pipeline steps passed!"
echo "================================"
echo ""
echo "Summary:"
echo "  - JAR built: target/composite-service-0.0.1-SNAPSHOT.jar"
echo "  - Tests: See target/surefire-reports/"
echo "  - Coverage: See target/site/jacoco/"
echo "  - Docker image: willbank-composite-service:test"
