/*
 * Copyright (c) 2023, Draque Thompson, draquemail@gmail.com
 * All rights reserved.
 *
 * Licensed under: MIT License
 * See LICENSE.TXT included with this code to read the full license agreement.

 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */
package org.darisadesigns.polyglotlina.Desktop;

import java.io.IOException;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 *
 * @author draquethompson
 */
public class PGTUtilTest {
    
    public PGTUtilTest() {
    }

    @Test
    public void testIsWriteLock_locked() throws Exception {
        try {
            PGTUtil.claimWriteLock();
            assertTrue(PGTUtil.isWriteLock());
        } catch (IOException e) {
            fail(e.getLocalizedMessage());
        } finally {
            PGTUtil.releaseWriteLock();
        }
    }
    
    @Test
    public void testIsWriteLock_unlocked() {
        assertFalse(PGTUtil.isWriteLock());
    }
    
    @Test
    public void testIsWriteLock_LockUnlock_isUnlocked() throws IOException {
        try {
            PGTUtil.claimWriteLock();
            PGTUtil.releaseWriteLock();
            assertFalse(PGTUtil.isWriteLock());
        } catch (IOException e) {
            fail(e.getLocalizedMessage());
        } finally {
            if (PGTUtil.isWriteLock()) {
                PGTUtil.releaseWriteLock();
            }
        }
    }
    
    @Test
    public void testClaimWriteLock_success() throws IOException {
        try {
            PGTUtil.claimWriteLock();
        } catch (IOException e) {
            fail(e.getLocalizedMessage());
        } finally {
            PGTUtil.releaseWriteLock();
        }
    }
    
    @Test()
    public void testClaimWriteLock_alreadyLocked_failure() throws IOException {
        try {
            PGTUtil.claimWriteLock();
            Exception exception = assertThrows(IOException.class, () ->
                PGTUtil.claimWriteLock());
            assertEquals("Write lock requested while locked.", exception.getMessage());
        } catch (IOException e) {
            fail(e.getLocalizedMessage());
        } finally {
            PGTUtil.releaseWriteLock();
        }
    }
    
    @Test
    public void releaseWriteLock_alreadyUnlocked() {
        Exception exception = assertThrows(IOException.class, () ->
            PGTUtil.releaseWriteLock());
        assertEquals("Write lock released without being claimed.", exception.getMessage());
    }
    
    @Test
    public void releaseWriteLock_wrongContext() throws IOException {
        try {
            PGTUtil.claimWriteLock();
            String exceptionMessage = changeCallStackContext().getLocalizedMessage();     
            assertTrue(exceptionMessage.startsWith("Writelock must be claimed and released from within the same method."));
        } catch (IOException e) {
            fail(e.getLocalizedMessage());
        } finally {
            if (PGTUtil.isWriteLock()) {
                PGTUtil.releaseWriteLock();
            }
        }
    }
    
    private Exception changeCallStackContext() {
        return assertThrows(IOException.class, () -> PGTUtil.releaseWriteLock());
    }
    
    @Test
    public void releaseWriteLock_success() throws IOException {
        try {
            PGTUtil.claimWriteLock();
            PGTUtil.releaseWriteLock();
            assertFalse(PGTUtil.isWriteLock());
        } catch (IOException e) {
            fail(e.getLocalizedMessage());
        } finally {
            if (PGTUtil.isWriteLock()) {
                PGTUtil.releaseWriteLock();
            }
        }
    }
    
    @Test
    public void waitForWritePermission_noActivity() {
        try {
            PGTUtil.waitForWritePermission();
        } catch (IOException e) {
            fail(e.getLocalizedMessage());
        }
    }
    
    @Test
    public void waitForWritePermission_timeout() throws IOException{
        try {
            PGTUtil.claimWriteLock();
            
            Exception exception = assertThrows(IOException.class, () ->
                PGTUtil.waitForWritePermission());
            assertTrue(exception.getLocalizedMessage().startsWith("Unable to obtain write permission after retrying for"));
        } catch (IOException e) {
            fail(e.getLocalizedMessage());
        } finally {
            if (PGTUtil.isWriteLock()) {
                PGTUtil.releaseWriteLock();
            }
        }
    }
    
    @Test
    public void waitForWritePermission_startBlockedUnblock() throws IOException {
        try {
            PGTUtil.claimWriteLock();
            
            Thread thread = new Thread() {
                @Override
                public void run() {
                    this.setName("FAIL");
                    
                    try {
                        PGTUtil.waitForWritePermission();
                        this.setName("SUCCESS");
                    } catch (IOException e) {
                        // do nothing, name indicates failure
                    }
                }
            };
            
            thread.start();
            
            Thread.sleep(PGTUtil.READ_PERMISSION_DELAY_MS);
            PGTUtil.releaseWriteLock();
            
            thread.join();
            
            assertEquals("SUCCESS", thread.getName());
        } catch (IOException | InterruptedException e) {
            fail(e.getLocalizedMessage());
        } finally {
            if (PGTUtil.isWriteLock()) {
                PGTUtil.releaseWriteLock();
            }
        }
    }
}
