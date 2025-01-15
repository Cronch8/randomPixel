
#include <SDL2/SDL_events.h>
#include <SDL2/SDL_keycode.h>
#include <SDL2/SDL_rect.h>
#include <SDL2/SDL_render.h>
#include <SDL2/SDL_stdinc.h>
#include <SDL2/SDL_timer.h>
#include <SDL2/SDL_video.h>
#include <stdint.h>
#include <stdio.h>
#include <SDL2/SDL.h>
#include <string.h>
#include <stdlib.h>


int main() {

    Uint32 width = 1920;
    Uint32 height = 1080;
    Uint32 entity_x = width/2;
    Uint32 entity_y = height/2;
    Uint32 running = 200; // how many times to run
    Uint32 frame_delay = 1000/60;
    Uint8 color[4] = {200, 255, 255, 255}; // RGBA
    Uint8 clear_color[4] = {0, 0, 0, 55}; // RGBA
    Uint32 entity_updates_per_draw = 10000;

    Uint32 entity_update_count = 0;
    SDL_Window* screen = NULL;
    SDL_Renderer* renderer;
    SDL_Event* event = NULL;
    SDL_Point points[entity_updates_per_draw];


    SDL_Init(SDL_INIT_EVERYTHING);
    SDL_CreateWindowAndRenderer(width, height, SDL_WINDOW_SHOWN, &screen, &renderer);

    printf("starting %d loops\n", running);
    while (running) {
        while (entity_update_count <= entity_updates_per_draw) {
            switch (random()%4) {
                case 0:
                    if (entity_x > width) entity_x = 0;
                    else entity_x++;
                    break;
                case 1:
                    if (entity_y > height) entity_y = 0;
                    else entity_y++;
                    break;
                case 2:
                    if (entity_x < 0) entity_x = width-1;
                    else entity_x--;
                    break;
                case 3:
                    if (entity_y < 0) entity_y = height-1;
                    else entity_y--;
                    break;
            }
            points[entity_update_count].x = entity_x;
            points[entity_update_count].y = entity_y;
            entity_update_count++;
        }


        entity_update_count = 0;
        if (SDL_PollEvent(event) && event != NULL && event->type == SDL_QUIT) {
            break;
        }
        SDL_SetRenderDrawColor(renderer, clear_color[0], clear_color[1], clear_color[2], clear_color[3]);
        SDL_RenderClear(renderer);
        SDL_SetRenderDrawColor(renderer, color[0], color[1], color[2], color[3]);
        SDL_RenderDrawPoints(renderer, points, entity_updates_per_draw);
        SDL_RenderPresent(renderer);
        running--;
        SDL_Delay(frame_delay);
    }
    printf("finished!\n");
}
