#!/bin/bash


# Define height ranges for each zoom level
# Example: If height is 500-599, zoom level is 0; 600-699, zoom level is 1, etc.
declare -A height_ranges
height_ranges[0]=500
height_ranges[1]=620
height_ranges[2]=710
# Add more ranges as needed

zoom_level=0

# Launch the terminal and run Java program
kitty java Spacerace &

# Give some time for the terminal to launch
sleep 1

# Obtain the window ID of the most recently opened kitty terminal
WINDOW_ID=$(xdotool search --class "kitty" | tail -1)

# Check if WINDOW_ID is set
if [ -z "$WINDOW_ID" ]; then
    echo "Error: Failed to obtain WINDOW_ID."
    exit 1
fi

# Adjust the size of the kitty window to the initial desired size
xdotool windowsize $WINDOW_ID 1020 500

# Initial zoom out commands
xdotool windowfocus $WINDOW_ID
for i in {1..4}; do
    xdotool key --window $WINDOW_ID Ctrl+Shift+minus
    sleep 0.25
done

# Check if an argument was provided
if [ $# -eq 1 ] && [ "$1" == "-l" ]; then
    echo "Loop mode, be patient with my bugs please."


# Continuously check window height and adjust zoom
    while true; do
        # Check if window is still valid
        if ! xdotool getwindowgeometry $WINDOW_ID &>/dev/null; then
            echo "Window is no longer valid."
            break
        fi

    # Get current window height
        CURRENT_HEIGHT=$(xdotool getwindowgeometry $WINDOW_ID | grep -oP 'Geometry: \K\d+x\d+' | cut -d'x' -f2)

        # Ensure CURRENT_HEIGHT is an integer
        if ! [[ "$CURRENT_HEIGHT" =~ ^[0-9]+$ ]]; then
            echo "Invalid height value."
            sleep 5
            continue
        fi

        if [ "$CURRENT_HEIGHT" -gt "${height_ranges[$zoom_level]}" ]; then
            if [ "$zoom_level" -lt 2 ]; then
                ((zoom_level++))
                xdotool key --window $WINDOW_ID Ctrl+Shift+plus
       # break
	            sleep 5
                continue
            fi
        elif [ "$CURRENT_HEIGHT" -lt "${height_ranges[$zoom_level]}" ]; then
            if [ "$zoom_level" -ge 0 ]; then
                ((zoom_level--))
                xdotool key --window $WINDOW_ID Ctrl+Shift+minus
       # break
	            sleep 5
                continue
            fi
        fi

    # Short sleep to prevent high CPU usage
        sleep 2.5
    done
fi
echo "Bye bye ^-^"
