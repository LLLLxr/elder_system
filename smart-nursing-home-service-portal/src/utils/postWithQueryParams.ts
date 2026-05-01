export type QueryParamValue = string | number | boolean | null | undefined;

export function buildPostUrl<T extends Record<string, QueryParamValue>>(
  path: string,
  params: T,
): string {
  const searchParams = new URLSearchParams();

  Object.entries(params).forEach(([key, value]) => {
    if (value !== undefined && value !== null && value !== '') {
      searchParams.append(key, String(value));
    }
  });

  const queryString = searchParams.toString();
  if (!queryString) {
    return path;
  }

  return `${path}?${queryString}`;
}
