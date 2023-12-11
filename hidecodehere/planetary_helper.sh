# Function to change the font size
change_font_size() {
    local size_change="$1"
    xdotool windowfocus $WINDOW_ID
    if [ $size_change -gt 0 ]; then
        for ((i = 0; i < $size_change; i++)); do
            xdotool key --window $WINDOW_ID Ctrl+Shift+plus
        done
    else
        for ((i = 0; i > $size_change; i--)); do
            xdotool key --window $WINDOW_ID Ctrl+Shift+minus
        done
    fi
}

# Function to resize the window
resize_window() {
    local width="$1"
    local height="$2"
    xdotool windowsize $WINDOW_ID $width $height
}

# Function to listen for key events
listen_for_key() {
    while true; do
        read -rsn1 key
        case "$key" in
            'F12') 
                change_font_size 1
                resize_window 1200 600
                ;;
            'F11') 
                change_font_size -1
                resize_window 1000 500
                ;;
            *)
                ;;
        esac
    done
}

# Ensure WINDOW_ID is defined before this point in your script
# Start listening for key events in the background
listen_for_key &


 
