
class Config {

    private static validateRequiredValue(key: string, value: string | undefined): void {
      if (!value) {
        throw new Error(`Environment variable ${key} is not set.`);
      }
    }
  

    static get undertowServerUri(): string {
        const uri = process.env.NEXT_PUBLIC_JCHESS_UNDERTOW_SERVER_URI;
        Config.validateRequiredValue('NEXT_PUBLIC_JCHESS_UNDERTOW_SERVER_URI', uri);
        return uri as string;
    }
  

    static get clientUri(): string {
        const uri = process.env.NEXT_PUBLIC_CLIENT_URI;
        Config.validateRequiredValue('NEXT_PUBLIC_CLIENT_URI', uri);
        return uri as string;
    }
  
    static get boardWithCoordinates(): boolean {
      return process.env.NEXT_PUBLIC_BOARD_WITH_COORDINATES === 'true';
    }
  
    static get useLocalStorage(): boolean {
      return process.env.NEXT_PUBLIC_LOCAL_STORAGE === 'true';
    }

    // log config values
    static log(): void {
      console.log('NEXT_PUBLIC_JCHESS_UNDERTOW_SERVER_URI', Config.undertowServerUri);
      console.log('NEXT_PUBLIC_CLIENT_URI', Config.clientUri);
      console.log('NEXT_PUBLIC_BOARD_WITH_COORDINATES', Config.boardWithCoordinates);
      console.log('NEXT_PUBLIC_LOCAL_STORAGE', Config.useLocalStorage);
    }
  }
  
  export default Config;
  