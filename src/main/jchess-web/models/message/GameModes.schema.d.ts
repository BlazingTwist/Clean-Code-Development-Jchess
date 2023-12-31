/* eslint-disable */
/**
 * This file was automatically generated by json-schema-to-typescript.
 * DO NOT MODIFY IT BY HAND. Instead, modify the source JSONSchema file,
 * and run json-schema-to-typescript to regenerate this file.
 */

export interface GameModes {
  modes: GameMode[];
  [k: string]: unknown;
}
export interface GameMode {
  /**
   * Id of the Game-Mode
   */
  modeId: string;
  displayName: string;
  numPlayers: number;
  /**
   * List of allowed themes for this Game-Mode
   */
  themeIds: string[];
  [k: string]: unknown;
}
