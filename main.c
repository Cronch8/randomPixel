#include <SDL2/SDL_blendmode.h>
#include <SDL2/SDL_events.h>
#include <SDL2/SDL_keycode.h>
#include <SDL2/SDL_pixels.h>
#include <SDL2/SDL_rect.h>
#include <SDL2/SDL_render.h>
#include <SDL2/SDL_stdinc.h>
#include <SDL2/SDL_timer.h>
#include <SDL2/SDL_video.h>
#include <signal.h>
#include <stdint.h>
#include <stdio.h>
#include <SDL2/SDL.h>
#include <string.h>
#include <stdlib.h>

Uint8 running = 1;

void handle_exit() {
    running = 0;
}

int main() {

    Uint32 width = 1920;
    Uint32 height = 1080;
    Uint32 entities[entity_count*2]; // formated as [ex_1, ey_1, ex_2, ey_2 ... ]
    Uint32 entity_x = width/2;
    Uint32 entity_y = height/2;
    Uint32 frame_delay = 1000/100; // FPS in milliseconds basically
    Uint8 color[4] = {200, 255, 255, 255}; // RGBA
    Uint8 clear_color[4] = {0, 0, 0, 255}; // RGBA
    Uint8 color_decay[4] = {1, 4, 2, 0}; // RGBA
    Uint32 entity_updates_per_draw = 20000;
    Uint32 entity_update_count = 0;

    SDL_Window* window;
    SDL_Renderer* renderer;
    SDL_Init(SDL_INIT_EVERYTHING);
    SDL_CreateWindowAndRenderer(width, height, SDL_WINDOW_SHOWN, &window, &renderer);
    SDL_Texture* texture = SDL_CreateTexture(renderer, SDL_PIXELFORMAT_ARGB8888, SDL_TEXTUREACCESS_STREAMING, width, height);
    printf("2"); fflush(stdout);
    SDL_Event* event = NULL;
    Uint8 image[width*4*height]; // formated as [r,g,b,a,r,g ... a,r,g,b,a]
    void* pixels;

    SDL_SetRenderDrawColor(renderer, clear_color[0], clear_color[1], clear_color[2], clear_color[3]);
    SDL_RenderClear(renderer);

    printf("Starting!\nuse ctrl+c in the terminal to close");
    while (running) {

        while (entity_update_count <= entity_updates_per_draw) {
            entity_x += random()%3 - 1;
            entity_y += random()%3 - 1;
            if (entity_x > width) {
                entity_x = 0;
            } else if (entity_x < 0) {
                entity_x = width;
            }
            if (entity_y > height) {
                entity_y = 0;
            } else if (entity_y < 0) {
                entity_y = height;
            }
            image[entity_x*4 + entity_y*width*4 + 0] = color[0];
            image[entity_x*4 + entity_y*width*4 + 1] = color[1];
            image[entity_x*4 + entity_y*width*4 + 2] = color[2];
            image[entity_x*4 + entity_y*width*4 + 3] = color[3];
            entity_update_count++;
        }

        for (Uint32 i = 0; i < width*4*height; i+=4) {
            image[i+0] = image[i+0] <= color_decay[0] ? image[i+0] : image[i+0] - color_decay[0];
            image[i+1] = image[i+1] <= color_decay[1] ? image[i+1] : image[i+1] - color_decay[1];
            image[i+2] = image[i+2] <= color_decay[2] ? image[i+2] : image[i+2] - color_decay[2];
            image[i+3] = image[i+3] <= color_decay[3] ? image[i+3] : image[i+3] - color_decay[3];
        }
        SDL_UpdateTexture(texture, NULL, image, width*4);
        //printf("loc x: %d, y: %d\n", entity_x, entity_y);

        entity_update_count = 0;
        signal(SIGTERM, handle_exit);
        signal(SIGINT, handle_exit);

        SDL_RenderClear(renderer);
        SDL_RenderCopy(renderer, texture, NULL, NULL);
        SDL_RenderPresent(renderer);
        SDL_Delay(frame_delay);
    }
    printf("finished!\n");
}
