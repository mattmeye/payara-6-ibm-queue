#!/bin/bash

# Load Test Runner für Payara 6 IBM MQ Integration
# Usage: ./run-load-tests.sh [rest|mq|combined|all] [gui|headless]

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Default values
TEST_TYPE=${1:-combined}
MODE=${2:-gui}
RESULTS_DIR="results"
TIMESTAMP=$(date +"%Y%m%d_%H%M%S")

# Helper functions
log_info() {
    echo -e "${BLUE}[INFO]${NC} $1"
}

log_success() {
    echo -e "${GREEN}[SUCCESS]${NC} $1"
}

log_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

log_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# Check prerequisites
check_prerequisites() {
    log_info "Checking prerequisites..."

    # Check if JMeter is installed
    if ! command -v jmeter &> /dev/null; then
        log_error "JMeter is not installed. Please install with: brew install jmeter"
        exit 1
    fi

    # Check if application is running
    if ! curl -s http://localhost:8080/payara6-ibmmq/api/simple/health &> /dev/null; then
        log_warning "Application not responding on http://localhost:8080"
        log_info "Please start the application with: mvn payara-micro:start"
        read -p "Continue anyway? (y/N): " -n 1 -r
        echo
        if [[ ! $REPLY =~ ^[Yy]$ ]]; then
            exit 1
        fi
    else
        log_success "Application is running"
    fi

    # Create results directory
    mkdir -p "$RESULTS_DIR"
}

# Run test function
run_test() {
    local test_file=$1
    local test_name=$2
    local result_file="$RESULTS_DIR/${test_name}_${TIMESTAMP}.jtl"
    local html_report="$RESULTS_DIR/${test_name}_${TIMESTAMP}_report"

    log_info "Running $test_name test..."

    if [ "$MODE" = "gui" ]; then
        log_info "Opening JMeter GUI for $test_name"
        jmeter -t "$test_file"
    else
        log_info "Running $test_name in headless mode"
        jmeter -n -t "$test_file" \
               -l "$result_file" \
               -e -o "$html_report"

        if [ $? -eq 0 ]; then
            log_success "$test_name completed successfully"
            log_info "Results saved to: $result_file"
            log_info "HTML report: $html_report/index.html"
        else
            log_error "$test_name failed"
            return 1
        fi
    fi
}

# Show usage
show_usage() {
    echo "Usage: $0 [TEST_TYPE] [MODE]"
    echo ""
    echo "TEST_TYPE options:"
    echo "  rest      - REST API load test only"
    echo "  mq        - IBM MQ load test only"
    echo "  combined  - Combined test (default)"
    echo "  all       - Run all tests sequentially"
    echo ""
    echo "MODE options:"
    echo "  gui       - Open JMeter GUI (default)"
    echo "  headless  - Run in command line mode"
    echo ""
    echo "Examples:"
    echo "  $0 combined gui     # Open combined test in GUI"
    echo "  $0 rest headless    # Run REST test headless"
    echo "  $0 all headless     # Run all tests headless"
}

# Validate system performance
check_system_performance() {
    if [ "$MODE" = "headless" ]; then
        log_info "Checking system performance during test..."

        # Monitor key metrics during headless runs
        cat > "$RESULTS_DIR/system_monitor_${TIMESTAMP}.sh" << 'EOF'
#!/bin/bash
while true; do
    echo "$(date): CPU: $(top -l 1 | grep "CPU usage" | awk '{print $3}'), Memory: $(top -l 1 | grep "PhysMem" | awk '{print $2}')" >> system_metrics.log
    sleep 10
done
EOF
        chmod +x "$RESULTS_DIR/system_monitor_${TIMESTAMP}.sh"

        # Start monitoring in background
        "$RESULTS_DIR/system_monitor_${TIMESTAMP}.sh" &
        MONITOR_PID=$!

        # Return monitor PID for cleanup
        echo $MONITOR_PID
    fi
}

# Cleanup function
cleanup() {
    if [ ! -z "$MONITOR_PID" ]; then
        log_info "Stopping system monitoring..."
        kill $MONITOR_PID 2>/dev/null || true
    fi
}

# Generate summary report
generate_summary() {
    if [ "$MODE" = "headless" ]; then
        log_info "Generating test summary..."

        cat > "$RESULTS_DIR/test_summary_${TIMESTAMP}.md" << EOF
# Load Test Summary - $(date)

## Test Configuration
- Test Type: $TEST_TYPE
- Mode: $MODE
- Timestamp: $TIMESTAMP

## Files Generated
EOF

        # List all generated files
        find "$RESULTS_DIR" -name "*${TIMESTAMP}*" -type f | while read file; do
            echo "- $(basename "$file")" >> "$RESULTS_DIR/test_summary_${TIMESTAMP}.md"
        done

        log_success "Summary generated: $RESULTS_DIR/test_summary_${TIMESTAMP}.md"
    fi
}

# Main execution
main() {
    # Show banner
    echo -e "${BLUE}"
    echo "=================================================="
    echo "  Payara 6 IBM MQ Load Test Runner"
    echo "=================================================="
    echo -e "${NC}"

    # Validate arguments
    if [ "$1" = "--help" ] || [ "$1" = "-h" ]; then
        show_usage
        exit 0
    fi

    if [[ ! "$TEST_TYPE" =~ ^(rest|mq|combined|all)$ ]]; then
        log_error "Invalid test type: $TEST_TYPE"
        show_usage
        exit 1
    fi

    if [[ ! "$MODE" =~ ^(gui|headless)$ ]]; then
        log_error "Invalid mode: $MODE"
        show_usage
        exit 1
    fi

    # Setup
    check_prerequisites

    # Start system monitoring if headless
    if [ "$MODE" = "headless" ]; then
        MONITOR_PID=$(check_system_performance)
        trap cleanup EXIT
    fi

    # Run tests based on type
    case $TEST_TYPE in
        rest)
            run_test "rest-api-load-test.jmx" "rest_api"
            ;;
        mq)
            run_test "ibm-mq-load-test.jmx" "ibm_mq"
            ;;
        combined)
            run_test "combined-load-test.jmx" "combined"
            ;;
        all)
            if [ "$MODE" = "gui" ]; then
                log_warning "Running all tests in GUI mode will open multiple windows"
                read -p "Continue? (y/N): " -n 1 -r
                echo
                if [[ ! $REPLY =~ ^[Yy]$ ]]; then
                    exit 1
                fi
            fi

            log_info "Running all tests..."
            run_test "rest-api-load-test.jmx" "rest_api"
            run_test "ibm-mq-load-test.jmx" "ibm_mq"
            run_test "combined-load-test.jmx" "combined"
            ;;
    esac

    # Generate summary
    generate_summary

    log_success "Load testing completed!"

    if [ "$MODE" = "headless" ]; then
        log_info "Check the $RESULTS_DIR directory for detailed results"
    fi
}

# Run main function
main "$@"