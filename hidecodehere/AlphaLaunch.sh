#!/bin/bash
# Launch the terminal and run Java program
kitty --start-as fullscreen  java Spacerace &
# Give some time for the terminal to launch
sleep 0.5

# Obtain the window ID of the most recently opened GNOME Terminal
WINDOW_ID=$(xdotool search --class "kitty" | tail -1)

# Use xdotool to send the zoom out command to the specific window
xdotool windowfocus $WINDOW_ID
xdotool key --window $WINDOW_ID Ctrl+Shift+minus
