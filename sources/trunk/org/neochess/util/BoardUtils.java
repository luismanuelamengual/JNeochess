
package org.neochess.util;

import org.neochess.engine2.Board;

public abstract class BoardUtils
{
    public static long NULLBITBOARD = 0x0000000000000000L;
    public static long[] squareBit;
    public static long[] squareBitX;
    public static long[] squareBit90;
    public static long[] squareBit45;
    public static long[] squareBit315;
    public static long[] squareBitX90;
    public static long[] squareBitX45;
    public static long[] squareBitX315;
    public static long[] rankBits;
    public static long[] fileBits;
    public static long[][] fromtoRay;
    public static long[][] moveArray;
    public static long[][] rook00Atak; 
    public static long[][] rook90Atak; 
    public static long[][] bishop45Atak;
    public static long[][] bishop315Atak;
    public static long[][] ray;
    public static long[][] distMap;
    public static int[][] distance;
    public static int[][] taxicab;
    public static int[][] directions;
    private static int[] lzArray;
    private static int[] bitCount;
    private static final int _ndir[] = { 2, 8, 4, 4, 8, 8, 2 };
    private static final int _range[] = { 0, 0, 1, 1, 1, 0, 0 };
    private static final int _slider[] = { 0, 0, 1, 1, 1, 0, 0 };
    
    private static final int _dir[][] =
    { {   9,  11,   0,  0, 0,  0,  0,  0 },
      { -21, -19, -12, -8, 8, 12, 19, 21 },
      { -11,  -9,   9, 11, 0,  0,  0,  0 },
      { -10,  -1,   1, 10, 0,  0,  0,  0 },
      { -11, -10,  -9, -1, 1,  9, 10, 11 },
      { -11, -10,  -9, -1, 1,  9, 10, 11 },
      {  -9, -11,   0,  0, 0,  0,  0,  0 } };
    
    private static final int _map[] =
    { -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
      -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
      -1,  0,  1,  2,  3,  4,  5,  6,  7, -1,
      -1,  8,  9, 10, 11, 12, 13, 14, 15, -1,
      -1, 16, 17, 18, 19, 20, 21, 22, 23, -1,
      -1, 24, 25, 26, 27, 28, 29, 30, 31, -1,
      -1, 32, 33, 34, 35, 36, 37, 38, 39, -1,
      -1, 40, 41, 42, 43, 44, 45, 46, 47, -1,
      -1, 48, 49, 50, 51, 52, 53, 54, 55, -1,
      -1, 56, 57, 58, 59, 60, 61, 62, 63, -1,
      -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
      -1, -1, -1, -1, -1, -1, -1, -1, -1, -1  };
   
    private static final int shift00[] =
    { 56, 56, 56, 56, 56, 56, 56, 56,
      48, 48, 48, 48, 48, 48, 48, 48,
      40, 40, 40, 40, 40, 40, 40, 40,
      32, 32, 32, 32, 32, 32, 32, 32,
      24, 24, 24, 24, 24, 24, 24, 24,
      16, 16, 16, 16, 16, 16, 16, 16,
       8,  8,  8,  8,  8,  8,  8,  8,
       0,  0,  0,  0,  0,  0,  0,  0 };

    private static final int shift90[] = 
    { 0, 8, 16, 24, 32, 40, 48, 56,
      0, 8, 16, 24, 32, 40, 48, 56,
      0, 8, 16, 24, 32, 40, 48, 56,
      0, 8, 16, 24, 32, 40, 48, 56,
      0, 8, 16, 24, 32, 40, 48, 56,
      0, 8, 16, 24, 32, 40, 48, 56,
      0, 8, 16, 24, 32, 40, 48, 56,
      0, 8, 16, 24, 32, 40, 48, 56 };
      
    private static final int shift45[] = 
    { 28, 36, 43, 49, 54, 58, 61, 63,
      21, 28, 36, 43, 49, 54, 58, 61,
      15, 21, 28, 36, 43, 49, 54, 58,
      10, 15, 21, 28, 36, 43, 49, 54,
       6, 10, 15, 21, 28, 36, 43, 49,
       3,  6, 10, 15, 21, 28, 36, 43,
       1,  3,  6, 10, 15, 21, 28, 36,
       0,  1,  3,  6, 10, 15, 21, 28 };

    private static final int shift315[] = 
    { 63, 61, 58, 54, 49, 43, 36, 28,
      61, 58, 54, 49, 43, 36, 28, 21,
      58, 54, 49, 43, 36, 28, 21, 15,
      54, 49, 43, 36, 28, 21, 15, 10,
      49, 43, 36, 28, 21, 15, 10,  6,
      43, 36, 28, 21, 15, 10,  6,  3,
      36, 28, 21, 15, 10,  6,  3,  1,
      28, 21, 15, 10,  6,  3,  1,  0 };
    
    private static final int mask45[] =
    { 0xFF, 0x7F, 0x3F, 0x1F, 0x0F, 0x07, 0x03, 0x01,
      0x7F, 0xFF, 0x7F, 0x3F, 0x1F, 0x0F, 0x07, 0x03, 
      0x3F, 0x7F, 0xFF, 0x7F, 0x3F, 0x1F, 0x0F, 0x07, 
      0x1F, 0x3F, 0x7F, 0xFF, 0x7F, 0x3F, 0x1F, 0x0F, 
      0x0F, 0x1F, 0x3F, 0x7F, 0xFF, 0x7F, 0x3F, 0x1F, 
      0x07, 0x0F, 0x1F, 0x3F, 0x7F, 0xFF, 0x7F, 0x3F, 
      0x03, 0x07, 0x0F, 0x1F, 0x3F, 0x7F, 0xFF, 0x7F, 
      0x01, 0x03, 0x07, 0x0F, 0x1F, 0x3F, 0x7F, 0xFF };

    private static final int mask315[] =
    { 0x01, 0x03, 0x07, 0x0F, 0x1F, 0x3F, 0x7F, 0xFF,
      0x03, 0x07, 0x0F, 0x1F, 0x3F, 0x7F, 0xFF, 0x7F,
      0x07, 0x0F, 0x1F, 0x3F, 0x7F, 0xFF, 0x7F, 0x3F,
      0x0F, 0x1F, 0x3F, 0x7F, 0xFF, 0x7F, 0x3F, 0x1F,
      0x1F, 0x3F, 0x7F, 0xFF, 0x7F, 0x3F, 0x1F, 0x0F,
      0x3F, 0x7F, 0xFF, 0x7F, 0x3F, 0x1F, 0x0F, 0x07,
      0x7F, 0xFF, 0x7F, 0x3F, 0x1F, 0x0F, 0x07, 0x03,
      0xFF, 0x7F, 0x3F, 0x1F, 0x0F, 0x07, 0x03, 0x01 };
    
    private static int r90[] = 
    {  56,  48, 40, 32, 24, 16, 8,  0,
       57,  49, 41, 33, 25, 17, 9,  1,
       58,  50, 42, 34, 26, 18, 10, 2,
       59,  51, 43, 35, 27, 19, 11, 3,
       60,  52, 44, 36, 28, 20, 12, 4,
       61,  53, 45, 37, 29, 21, 13, 5,
       62,  54, 46, 38, 30, 22, 14, 6,
       63,  55, 47, 39, 31, 23, 15, 7  };
    
    private static int r45[] =
    {  28, 21, 15, 10,  6,  3,  1,  0,
       36, 29, 22, 16, 11,  7,  4,  2,
       43, 37, 30, 23, 17, 12,  8,  5,
       49, 44, 38, 31, 24, 18, 13,  9,
       54, 50, 45, 39, 32, 25, 19, 14,
       58, 55, 51, 46, 40, 33, 26, 20,
       61, 59, 56, 52, 47, 41, 34, 27,
       63, 62, 60, 57, 53, 48, 42, 35  };

    private static int r315[] =
    {  0,  2,  5,  9, 14, 20, 27, 35,
       1,  4,  8, 13, 19, 26, 34, 42,
       3,  7, 12, 18, 25, 33, 41, 48,
       6, 11, 17, 24, 32, 40, 47, 53,
      10, 16, 23, 31, 39, 46, 52, 57,
      15, 22, 30, 38, 45, 51, 56, 60,
      21, 29, 37, 44, 50, 55, 59, 62,
      28, 36, 43, 49, 54, 58, 61, 63  };
    
    public static int getLeastSignificantBit (long bitboard) 
    {
        if ((bitboard >>> 48) > 0) 
            return lzArray[(int)(bitboard >>> 48)];
        if ((bitboard >>> 32) > 0) 
            return lzArray[ (int)(bitboard >>> 32)] + 16;
        if ((bitboard >>> 16) > 0) 
            return lzArray[ (int)(bitboard >>> 16)] + 32;
        return lzArray[(int)(bitboard)] + 48;
    }
    
    public static int getMostSignificantBit (long bitboard)
    {
        return getLeastSignificantBit(bitboard & ~ ((bitboard)&(bitboard-1)));
    }
    
    public static int getBitCount (long bitboard)
    {
        return bitCount[(int)(bitboard>>>48)] + bitCount[(int)((bitboard>>>32) & 0xffff)] + bitCount[(int)((bitboard>>>16) & 0xffff)] + bitCount[(int)(bitboard & 0xffff)];
    }
    
    public static void print (long bitboard)
    {
        System.out.println();
        for (int rank = 7; rank >= 0; rank--)
        {
            for (int file = 0; file <= 7; file++)
            {
                System.out.print(" "); 
                System.out.print(((bitboard & squareBit[rank*8+file]) != 0)? 1 : 0);
            }
            System.out.println();
        }
    }
    
    private static void initSquareBitArrays () 
    {
        squareBit = new long[64];
        squareBitX = new long[64];
        squareBit90 = new long[64];
        squareBit45 = new long[64];
        squareBit315 = new long[64];
        squareBitX90 = new long[64];
        squareBitX45 = new long[64];
        squareBitX315 = new long[64];
        long b = (long)1;  
        for (int i = 63; i >= 0; i--, b <<= 1) 
        {
            squareBit[i] = b;
            squareBitX[i] = ~b;
        }
        for (int i = 0; i < 64; i++) 
        {
            squareBit90[i] = squareBit[r90[i]];
            squareBit45[i] = squareBit[r45[i]];
            squareBit315[i] = squareBit[r315[i]];
            squareBitX90[i] = squareBitX[r90[i]];
            squareBitX45[i] = squareBitX[r45[i]];
            squareBitX315[i] = squareBitX[r315[i]];
        }
    }
    
    private static void initRankFileArrays () 
    {
        rankBits = new long[8];
        fileBits = new long[8];
        int i = 8;
        long b = (long)255;
        while (i-- != 0) 
        {
            rankBits[i] = b;
            b <<= 8;
        }
        i = 8;   
        b = 0x0101010101010101L;
        while (i-- != 0) 
        {
            fileBits[i] = b;
            b <<= 1;
        }
    }
    
    private static void initLzArray ()
    {
        lzArray = new int[65536];
        int i, j, s, n;
        s = n = 1;
        for (i = 0; i < 16; i++) 
        {
            for (j = s; j < s + n; j++)
                lzArray[j] = 16 - 1 - i;
            s += n;
            n += n;
        }
    }
    
    private static void initBitCount ()
    {
        bitCount = new int[65536];
        int i, j, n;
        bitCount[0] = 0;
        bitCount[1] = 1; 
        i = 1;
        for (n = 2; n <= 16; n++) 
        {
            i <<= 1;
            for (j = i; j <= i + (i-1); j++) 
                bitCount[j] = 1 + bitCount[j - i]; 
        }
    }
    
    private static void _initMoveArray () 
    {
        moveArray = new long[7][64];
        for (byte pieceRef = Board.PAWN; pieceRef <= Board.BPAWN; pieceRef++) 
        {
            for (byte mapSquare = 0; mapSquare < 120; mapSquare++) 
            {
                int from = _map[mapSquare];
                if (from == -1) continue;
                moveArray[pieceRef][from] = NULLBITBOARD;
                for (byte n = 0; n < _ndir[pieceRef]; n++)
                {
                    int to = from;
                    do 
                    {
                        to += _dir[pieceRef][n];
                        if ((to = _map[to]) != -1) 
                            moveArray[pieceRef][from] |= squareBit[to];
                    } 
                    while (( _range[pieceRef] > 0 ) && to != -1);
                }
            }
        }
    }
    
    /**
     * Inicializa el Array de Movimientos para las piezas 
     * @access private 
     */
    private static void _initFromtoRay () 
    {
        int pieceRef, from, to, f, t, n;
        fromtoRay = new ncBitBoard[64][64];
        for ( f = ncBoard.a1; f <= ncBoard.h8; f++ ) {
            for ( t = ncBoard.a1; t <= ncBoard.h8; t++ ) {
                fromtoRay[f][t] = new ncBitBoard();
            }
        }
        long moves;
	for (pieceRef = BITBOARD_BISHOP; pieceRef <= BITBOARD_ROOK; pieceRef++) {
            for (from = 0; from < 120; from++) {
                if ((f = _map[from]) == -1) continue;
                for (n = 0; n < _ndir[pieceRef]; n++) {
                    to = from;
                    t = _map[to];
                    do {
                        moves = fromtoRay[f][t].value;
                        to += _dir[pieceRef][n];         
                        if ((t = _map[to]) != -1)
                        {
                            fromtoRay[f][t].setBit( t );
                            fromtoRay[f][t].value |= moves;
                        }
                    } while (t != -1);
                }
            }	
	}
    }
    
    /**
     * Inicializa los Bitboard de ataque asi como tambien los bitboard de ataque
     * rotados 90, 45 y 315 grados
     */
    private static void _initRotatedAtacks ()
    {
	int sq, map, sq1, sq2;
	int cmap[] = { 128, 64, 32, 16, 8, 4, 2, 1 };
	int rot1[] = { ncBoard.a1, ncBoard.a2, ncBoard.a3, ncBoard.a4, ncBoard.a5, ncBoard.a6, ncBoard.a7, ncBoard.a8 };
	int rot2[] = { ncBoard.a1, ncBoard.b2, ncBoard.c3, ncBoard.d4, ncBoard.e5, ncBoard.f6, ncBoard.g7, ncBoard.h8 };
	int rot3[] = { ncBoard.a8, ncBoard.b7, ncBoard.c6, ncBoard.d5, ncBoard.e4, ncBoard.f3, ncBoard.g2, ncBoard.h1 };

        //Inicializacion de los arrays de BitBoards
        rook00Atak = new ncBitBoard[64][256]; 
        rook90Atak = new ncBitBoard[64][256]; 
        bishop45Atak = new ncBitBoard[64][256];
        bishop315Atak = new ncBitBoard[64][256];
        for (sq = ncBoard.a1; sq <= ncBoard.h8; sq++) {
            for (map = 0; map < 256; map++) {
                rook00Atak[sq][map] = new ncBitBoard();
                rook90Atak[sq][map] = new ncBitBoard();
                bishop45Atak[sq][map] = new ncBitBoard();
                bishop315Atak[sq][map] = new ncBitBoard();
            }
        }
        
        for (sq = ncBoard.a1; sq <= ncBoard.h1; sq++) {
            for (map = 0; map < 256; map++) {
                rook00Atak[sq][map].value = 0; 
                rook90Atak[sq][map].value = 0;
                bishop45Atak[sq][map].value = 0;
                bishop315Atak[sq][map].value = 0;
                sq1 = sq2 = sq;
                while (sq1 > 0) { if ( ( cmap[--sq1] & map ) > 0 ) break; }
                while (sq2 < 7) { if ( ( cmap[++sq2] & map ) > 0 ) break; }  
                rook00Atak[sq][map].value = fromtoRay[sq][sq1].value | fromtoRay[sq][sq2].value;
                rook90Atak[rot1[sq]][map].value = fromtoRay[rot1[sq]][rot1[sq1]].value | fromtoRay[rot1[sq]][rot1[sq2]].value;
                bishop45Atak[rot2[sq]][map].value = fromtoRay[rot2[sq]][rot2[sq1]].value | fromtoRay[rot2[sq]][rot2[sq2]].value;
                bishop315Atak[rot3[sq]][map].value = fromtoRay[rot3[sq]][rot3[sq1]].value | fromtoRay[rot3[sq]][rot3[sq2]].value;
            }
	} 

	for (map = 0; map < 256; map++) {
            for (sq = ncBoard.a2; sq <= ncBoard.h8; sq++) {
                rook00Atak[sq][map].value = rook00Atak[sq-8][map].value >>> 8;
            }
            for (sq1 = ncBoard.B; sq1 <= ncBoard.H; sq1++) {
                for (sq2 = ncBoard.a1; sq2 <= ncBoard.h8; sq2+=8) {
                    sq = sq2 + sq1;
                    rook90Atak[sq][map].value = rook90Atak[sq-1][map].value >>> 1;
                }
            }
            for (sq1 = ncBoard.b1, sq2 = ncBoard.h7; sq1 <= ncBoard.h1; sq1++, sq2-=8) {
                for (sq = sq1; sq <= sq2; sq += 9) {
                    bishop45Atak[sq][map].value = bishop45Atak[sq+8][map].value << 8;
                }
            }
            for (sq1 = ncBoard.a2, sq2 = ncBoard.g8; sq1 <= ncBoard.a8; sq1+=8, sq2--) {
                for (sq = sq1; sq <= sq2; sq += 9) {
                    bishop45Atak[sq][map].value = (bishop45Atak[sq+1][map].value & ncBitBoard.squareBitX[sq1-8]) << 1;	
                }
            }
            for (sq1 = ncBoard.h2, sq2 = ncBoard.b8; sq1 <= ncBoard.h8; sq1+=8, sq2++) {
                for (sq = sq1; sq <= sq2; sq += 7) {
                    bishop315Atak[sq][map].value = bishop315Atak[sq-8][map].value >>> 8;
                }
            }
            for (sq1 = ncBoard.g1, sq2 = ncBoard.a7; sq1 >= ncBoard.a1; sq1--, sq2-=8) {
                for (sq = sq1; sq <= sq2; sq += 7) {
                    bishop315Atak[sq][map].value = (bishop315Atak[sq+1][map].value & ncBitBoard.squareBitX[sq2+8]) << 1;
                }
            }
	}
    }
    
    /**
     * Inicializa los desplazamientos desde un cuadro en una direccion establecida
     */
    private static void _initRayArray ()
    {
        int square, direction;
        int piece, fsq, tsq, f, t, n, tempray;
        
        ray = new ncBitBoard[64][8];
        for ( square = ncBoard.a1; square <= ncBoard.h8; square++ ) 
            for ( direction = 0; direction < 8; direction++ )
                ray[square][direction] = new ncBitBoard();
        
        for ( f = ncBoard.a1; f <= ncBoard.h8; f++ ) 
            for ( t = ncBoard.a1; t <= ncBoard.h8; t++ ) 
                directions[f][t] = -1;
        
	for (fsq = 0; fsq < 120; fsq++)
	{
            if ((f = _map[fsq]) == -1) continue;
            tempray = -1;
            for (piece = ncGlobals.BISHOP; piece <= ncGlobals.ROOK; piece++)
            {
                for (n = 0; n < _ndir[piece]; n++)
                {
                    tempray += 1;
                    ray[f][tempray].clear();
                    tsq = fsq;
                    do
                    {
                        tsq += _dir[piece][n];
                        if ((t = _map[tsq]) != -1)
                        {
                            ray[f][tempray].setBit(t);
                            directions[f][t] = tempray;
                        }
                    } while (t != -1);
                }
            }	
	}
    }
    
    /**
     * Inicializa los arrays de Distancia
     */
    private static void _initDistanceArrays ()
    {
        int f, t, j;
	int d1, d2;

        distMap = new ncBitBoard[64][8];
	for (f = 0; f < 64; f++)
            for (t = 0; t < 8; t++)
            {
		distMap[f][t] = new ncBitBoard();
                distMap[f][t].clear();
            }

	for (f = 0; f < 64; f++)
            for (t = f; t < 64; t++)
            {
                d1 = (t & 0x07) - (f & 0x07);
                if (d1 < 0) d1 = -d1;
                d2 = (t >> 3) - (f >> 3);
                if (d2 < 0) d2 = -d2;
                distance[f][t] = Math.max(d1, d2);
                distance[t][f] = Math.max(d1, d2);
                taxicab[f][t] = d1 + d2;
                taxicab[t][f] = d1 + d2;
            }

	for (f = 0; f < 64; f++)
            for (t = 0; t < 64; t++)
                distMap[f][distance[t][f]].value |= ncBitBoard.squareBit[t];

	for (f = 0; f < 64; f++)
            for (t = 0; t < 8; t++)
                for (j = 0; j < t; j++)
                    distMap[f][t].value |= distMap[f][j].value;
    }
    
    static 
    {
        initSquareBitArrays ();
        initRankFileArrays ();
        initLzArray ();
        initBitCount ();
        _initFromtoRay ();
        _initMoveArray ();
        _initRotatedAtacks ();
        _initDistanceArrays ();
        _initRayArray ();
    }
}
