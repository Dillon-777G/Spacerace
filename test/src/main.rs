use chrono::{Local, TimeZone}; // Removed Duration from chrono
use crossterm::{
    event::{self, DisableMouseCapture, EnableMouseCapture, Event as CEvent, KeyCode},
    execute,
    terminal::{disable_raw_mode, enable_raw_mode, EnterAlternateScreen, LeaveAlternateScreen},
};
use rand::Rng;
use ratatui::{
    backend::CrosstermBackend,
    layout::{Constraint, Direction, Layout},
    widgets::{Block, Borders, Paragraph},
    Frame, Terminal,
};
use std::collections::BTreeMap;
use std::fs;
use std::io::{stdout, Result};
use std::thread;
use std::time::{Duration, Instant}; // This is from std::time, used for thread::sleep

struct App {
    art_lines: Vec<String>,
    degree_art_map: BTreeMap<i32, String>,
    // Other fields as needed
}

fn calculate_earth_position() -> f64 {
    let reference_date = Local.ymd(2000, 1, 1).and_hms(0, 0, 0);
    let now = Local::now();
    let days_between = now.signed_duration_since(reference_date).num_days();
    (days_between as f64 * 360.0 / 365.25) % 360.0
}

struct SpaceRace {
    art_lines: Vec<String>,
    degree_art_map: BTreeMap<i32, String>,
}

impl SpaceRace {
    fn new() -> Self {
        let mut degree_art_map = BTreeMap::new();
        degree_art_map.insert(0, "asciiArt/space010.txt".to_string());
        degree_art_map.insert(20, "asciiArt/space2030.txt".to_string());
        degree_art_map.insert(40, "asciiArt/space4050.txt".to_string());
        degree_art_map.insert(60, "asciiArt/space6070.txt".to_string());
        degree_art_map.insert(90, "asciiArt/space90110.txt".to_string());
        degree_art_map.insert(120, "asciiArt/space120130.txt".to_string());
        degree_art_map.insert(140, "asciiArt/space140150.txt".to_string());
        degree_art_map.insert(160, "asciiArt/space160170.txt".to_string());
        degree_art_map.insert(180, "asciiArt/space180190.txt".to_string());
        degree_art_map.insert(200, "asciiArt/space200210.txt".to_string());
        degree_art_map.insert(220, "asciiArt/space220230.txt".to_string());
        degree_art_map.insert(240, "asciiArt/space240260.txt".to_string());
        degree_art_map.insert(270, "asciiArt/space270290.txt".to_string());
        degree_art_map.insert(300, "asciiArt/space300310.txt".to_string());
        degree_art_map.insert(320, "asciiArt/space32030.txt".to_string());
        degree_art_map.insert(340, "asciiArt/space34050.txt".to_string());

        Self {
            art_lines: Vec::new(),
            degree_art_map,
        }
    }

    fn load_ascii_art_for_current_position(&mut self, position: f64) {
        if let Some((_, file_path)) = self.degree_art_map.range(..position as i32).next_back() {
            if let Ok(lines) = fs::read_to_string(file_path) {
                self.art_lines = lines.lines().map(String::from).collect();
            } else {
                println!("Error loading ASCII art");
            }
        }
    }
}

impl SpaceRace {
    // Method to update the twinkling effect once
    fn update_twinkling(&mut self) {
        let original_art = self.art_lines.clone();
        let mut rng = rand::thread_rng();

        for line in self.art_lines.iter_mut() {
            let mut new_line = String::with_capacity(line.len());
            for c in line.chars() {
                match c {
                    '*' => {
                        // Simulate twinkling by randomly choosing a character
                        let choices = ['*', '+', '.', ' '];
                        new_line.push(choices[rng.gen_range(0..choices.len())]);
                    }
                    '┼' => {
                        let choices = ['┼', '├', '─', ' '];
                        new_line.push(choices[rng.gen_range(0..choices.len())]);
                    }
                    _ => new_line.push(c),
                }
            }
            *line = new_line;
        }

        // No need to manually clear the screen or sleep here, as TUI handles rendering
    }
}

fn main() {
    let mut space_race = SpaceRace::new();
    let position = calculate_earth_position();

    space_race.load_ascii_art_for_current_position(position);
    space_race.start_twinkling();
}