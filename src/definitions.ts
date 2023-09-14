export interface SumUpOCPlugin {
  echo(options: { value: string }): Promise<{ value: string }>;
}
