#!/bin/bash

# IBM MQ Integration Test Runner
# This script runs different types of tests for the Payara 6 IBM MQ Integration project

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Functions
print_header() {
    echo -e "\n${BLUE}===========================================${NC}"
    echo -e "${BLUE}$1${NC}"
    echo -e "${BLUE}===========================================${NC}\n"
}

print_success() {
    echo -e "${GREEN}✅ $1${NC}"
}

print_error() {
    echo -e "${RED}❌ $1${NC}"
}

print_warning() {
    echo -e "${YELLOW}⚠️  $1${NC}"
}

print_info() {
    echo -e "${BLUE}ℹ️  $1${NC}"
}

# Parse command line arguments
TEST_TYPE="all"
CLEAN=false
SKIP_BUILD=false
VERBOSE=false

while [[ $# -gt 0 ]]; do
    case $1 in
        -t|--type)
            TEST_TYPE="$2"
            shift 2
            ;;
        -c|--clean)
            CLEAN=true
            shift
            ;;
        -s|--skip-build)
            SKIP_BUILD=true
            shift
            ;;
        -v|--verbose)
            VERBOSE=true
            shift
            ;;
        -h|--help)
            echo "Usage: $0 [OPTIONS]"
            echo ""
            echo "Options:"
            echo "  -t, --type TYPE     Test type: unit, integration, performance, all (default: all)"
            echo "  -c, --clean         Clean before running tests"
            echo "  -s, --skip-build    Skip build phase"
            echo "  -v, --verbose       Verbose output"
            echo "  -h, --help          Show this help message"
            echo ""
            echo "Test Types:"
            echo "  unit                Run unit tests only"
            echo "  integration         Run integration tests only"
            echo "  performance         Run performance tests only"
            echo "  all                 Run all tests (default)"
            echo ""
            echo "Examples:"
            echo "  $0                  # Run all tests"
            echo "  $0 -t unit          # Run only unit tests"
            echo "  $0 -t integration -v # Run integration tests with verbose output"
            echo "  $0 -c -t all        # Clean and run all tests"
            exit 0
            ;;
        *)
            print_error "Unknown option: $1"
            exit 1
            ;;
    esac
done

print_header "Payara 6 IBM MQ Integration - Test Runner"

# Check Java version
print_info "Checking Java version..."
if command -v asdf > /dev/null; then
    print_info "Using asdf for Java version management"
    asdf current java
else
    java -version
fi

# Clean if requested
if [ "$CLEAN" = true ]; then
    print_info "Cleaning project..."
    mvn clean
fi

# Build if not skipped
if [ "$SKIP_BUILD" = false ]; then
    print_info "Building project..."
    if [ "$VERBOSE" = true ]; then
        mvn compile test-compile
    else
        mvn compile test-compile -q
    fi
fi

# Set Maven options
MAVEN_OPTS=""
if [ "$VERBOSE" = false ]; then
    MAVEN_OPTS="$MAVEN_OPTS -q"
fi

# Run tests based on type
case $TEST_TYPE in
    "unit")
        print_header "Running Unit Tests"
        if mvn $MAVEN_OPTS test; then
            print_success "Unit tests passed"
        else
            print_error "Unit tests failed"
            exit 1
        fi
        ;;

    "integration")
        print_header "Running Integration Tests"
        print_info "Starting Docker containers for integration tests..."

        # Check if Docker is running
        if ! docker info > /dev/null 2>&1; then
            print_error "Docker is not running. Please start Docker and try again."
            exit 1
        fi

        # Start test containers
        docker-compose -f docker-compose.yml up -d postgres ibmmq

        # Wait for containers to be ready
        print_info "Waiting for containers to be ready..."
        sleep 30

        if mvn $MAVEN_OPTS verify -Dskip.unit.tests=true; then
            print_success "Integration tests passed"
        else
            print_error "Integration tests failed"
            docker-compose -f docker-compose.yml down
            exit 1
        fi

        # Cleanup
        print_info "Stopping test containers..."
        docker-compose -f docker-compose.yml down
        ;;

    "performance")
        print_header "Running Performance Tests"
        if mvn $MAVEN_OPTS test -Dtest="**/*PerformanceTest"; then
            print_success "Performance tests passed"
        else
            print_error "Performance tests failed"
            exit 1
        fi
        ;;

    "all")
        print_header "Running All Tests"

        # Unit tests
        print_info "Running unit tests..."
        if mvn $MAVEN_OPTS test; then
            print_success "Unit tests passed"
        else
            print_error "Unit tests failed"
            exit 1
        fi

        # Performance tests (part of unit test phase)
        print_info "Performance tests included in unit test phase"

        # Integration tests
        print_info "Running integration tests..."
        print_info "Starting Docker containers..."

        if ! docker info > /dev/null 2>&1; then
            print_warning "Docker is not running. Skipping integration tests."
        else
            docker-compose -f docker-compose.yml up -d postgres ibmmq
            sleep 30

            if mvn $MAVEN_OPTS verify -Dskip.unit.tests=true; then
                print_success "Integration tests passed"
            else
                print_error "Integration tests failed"
                docker-compose -f docker-compose.yml down
                exit 1
            fi

            docker-compose -f docker-compose.yml down
        fi
        ;;

    *)
        print_error "Unknown test type: $TEST_TYPE"
        print_info "Valid types: unit, integration, performance, all"
        exit 1
        ;;
esac

# Generate test report
print_header "Test Summary"

if [ -f "target/surefire-reports/TEST-*.xml" ]; then
    UNIT_TESTS=$(find target/surefire-reports -name "TEST-*.xml" | wc -l)
    print_info "Unit test reports: $UNIT_TESTS files generated"
fi

if [ -f "target/failsafe-reports/TEST-*.xml" ]; then
    INTEGRATION_TESTS=$(find target/failsafe-reports -name "TEST-*.xml" | wc -l)
    print_info "Integration test reports: $INTEGRATION_TESTS files generated"
fi

print_success "All tests completed successfully!"

# Coverage report (if available)
if [ -f "target/site/jacoco/index.html" ]; then
    print_info "Code coverage report available at: target/site/jacoco/index.html"
fi

print_info "Test reports available in target/surefire-reports/ and target/failsafe-reports/"

print_header "Test Run Complete"
echo -e "${GREEN}All requested tests have been executed successfully!${NC}"